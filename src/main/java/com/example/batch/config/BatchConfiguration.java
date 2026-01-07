package com.example.batch.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.batch.dto.OldCompanyDto;
import com.example.batch.dto.OldCustomerDto;
import com.example.batch.entity.newdb.NewCompany;
import com.example.batch.entity.newdb.NewCustomer;
import com.example.batch.listener.JobCompletionNotificationListener;
import com.example.batch.writer.UpsertItemWriter;

/**
 * Spring Batchのバッチジョブ設定クラス。
 *
 * <p>
 * このクラスは、データ移行バッチジョブの以下のコンポーネントを定義します：
 * </p>
 * <ul>
 * <li>TSVファイルからデータを読み込むReader（FlatFileItemReader）</li>
 * <li>データを変換するProcessor（ItemProcessor実装クラス）</li>
 * <li>データベースに書き込むWriter（JpaItemWriter）</li>
 * <li>Reader-Processor-Writerを組み合わせたStep</li>
 * <li>複数のStepを順序実行するJob</li>
 * </ul>
 *
 * <p>
 * バッチジョブの実行フロー：
 * </p>
 * 
 * <pre>
 * 1. customerMigrationStep
 *    TSVファイル読込 → データ変換 → DB書込（顧客データ）
 * 2. companyMigrationStep
 *    TSVファイル読込 → データ変換 → DB書込（会社データ）
 * </pre>
 *
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 * @see org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
 */
@Configuration
public class BatchConfiguration {

    /** 顧客データTSVファイルのパス */
    @Value("${batch.input.customer-file}")
    private Resource customerInputResource;

    /** 会社データTSVファイルのパス */
    @Value("${batch.input.company-file}")
    private Resource companyInputResource;

    /** チャンクサイズ */
    @Value("${batch.chunk-size}")
    private int chunkSize;

    /** スキップ上限 */
    @Value("${batch.skip-limit}")
    private int skipLimit;

    // ========== 顧客移行設定 ==========

    /**
     * 顧客データTSVファイルを読み込むReaderを生成します。
     *
     * <p>
     * このReaderは以下の処理を実行します：
     * </p>
     * <ul>
     * <li>TSVファイル（タブ区切り）の読み込み</li>
     * <li>1行目（ヘッダー行）のスキップ</li>
     * <li>各フィールドをOldCustomerDtoにマッピング</li>
     * <li>日付時刻の文字列からLocalDateTimeへの変換</li>
     * <li>性別コードの文字列からIntegerへの変換</li>
     * </ul>
     *
     * @return 顧客データを読み込むFlatFileItemReader
     */
    @Bean
    public FlatFileItemReader<OldCustomerDto> oldCustomerTsvReader() {
        return new FlatFileItemReaderBuilder<OldCustomerDto>()
                .name("oldCustomerTsvReader")
                .resource(customerInputResource)
                .linesToSkip(1)
                .delimited()
                .delimiter("\t")
                .names("customerCode", "customerName", "email", "phone", "address",
                        "postalCode", "createdAt", "status", "genderCode")
                .fieldSetMapper(fieldSet -> {
                    OldCustomerDto dto = new OldCustomerDto();
                    dto.setCustomerCode(fieldSet.readString("customerCode"));
                    dto.setCustomerName(fieldSet.readString("customerName"));
                    dto.setEmail(fieldSet.readString("email"));
                    dto.setPhone(fieldSet.readString("phone"));
                    dto.setAddress(fieldSet.readString("address"));
                    dto.setPostalCode(fieldSet.readString("postalCode"));

                    String createdAtStr = fieldSet.readString("createdAt");
                    if (createdAtStr != null && !createdAtStr.isEmpty()) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        dto.setCreatedAt(LocalDateTime.parse(createdAtStr, formatter));
                    }

                    dto.setStatus(fieldSet.readString("status"));

                    String genderCodeStr = fieldSet.readString("genderCode");
                    if (genderCodeStr != null && !genderCodeStr.isEmpty()) {
                        dto.setGenderCode(Integer.parseInt(genderCodeStr));
                    }

                    return dto;
                })
                .build();
    }

    /**
     * 新顧客データをデータベースに書き込むWriterを生成します。
     *
     * <p>
     * このWriterは、JPAのEntityManagerを使用してデータベースに
     * エンティティを永続化します。
     * </p>
     *
     * @param newEntityManagerFactory 新データベース用のEntityManagerFactory
     * @return 顧客データを書き込むJpaItemWriter
     */
    @Bean
    @StepScope
    public ItemWriter<NewCustomer> newCustomerWriter(
            @Qualifier("newEntityManagerFactory") EntityManagerFactory newEntityManagerFactory,
            @Value("#{jobParameters['upsertEnabled']}") String upsertEnabled) {

        if ("true".equalsIgnoreCase(upsertEnabled)) {
            // SharedEntityManagerCreator creates a transactional EntityManager proxy
            EntityManager sharedEntityManager = org.springframework.orm.jpa.SharedEntityManagerCreator
                    .createSharedEntityManager(newEntityManagerFactory);
            return new UpsertItemWriter<>(sharedEntityManager);
        }

        return new JpaItemWriterBuilder<NewCustomer>()
                .entityManagerFactory(newEntityManagerFactory)
                .build();
    }

    /**
     * 顧客データ移行ステップを生成します。
     *
     * <p>
     * このステップは、以下の処理をチャンク単位（100件ずつ）で実行します：
     * </p>
     * <ol>
     * <li>TSVファイルから顧客データを読み込み（Reader）</li>
     * <li>旧形式から新形式にデータを変換（Processor）</li>
     * <li>新データベースに書き込み（Writer）</li>
     * </ol>
     *
     * @param jobRepository        バッチジョブのメタデータを管理するリポジトリ
     * @param transactionManager   トランザクション管理用マネージャー
     * @param oldCustomerTsvReader 顧客データTSVリーダー
     * @param customerProcessor    顧客データ変換プロセッサ
     * @param newCustomerWriter    顧客データライター
     * @return 顧客データ移行ステップ
     */
    @Bean
    public Step customerMigrationStep(JobRepository jobRepository,
            @Qualifier("newTransactionManager") PlatformTransactionManager transactionManager,
            FlatFileItemReader<OldCustomerDto> oldCustomerTsvReader,
            @Qualifier("effectiveCustomerProcessor") ItemProcessor<OldCustomerDto, NewCustomer> customerProcessor,
            ItemWriter<NewCustomer> newCustomerWriter,
            com.example.batch.listener.CustomSkipListener<OldCustomerDto, NewCustomer> skipListener,
            com.example.batch.listener.ProgressListener<OldCustomerDto, NewCustomer> progressListener) {
        return new StepBuilder("customerMigrationStep", jobRepository)
                .<OldCustomerDto, NewCustomer>chunk(chunkSize, transactionManager)
                .reader(oldCustomerTsvReader)
                .processor(customerProcessor)
                .writer(newCustomerWriter)
                // エラーハンドリング設定
                .faultTolerant()
                .skip(Exception.class) // 全ての例外をスキップ対象に
                .skipLimit(skipLimit) // application.ymlで設定可能
                .retry(org.springframework.dao.DeadlockLoserDataAccessException.class)
                .retry(org.springframework.dao.TransientDataAccessException.class)
                .retryLimit(3) // 最大3回リトライ
                .listener(skipListener)
                // 進捗監視
                .listener((ChunkListener) progressListener)
                .listener((ItemReadListener<OldCustomerDto>) progressListener)
                .listener((ItemWriteListener<NewCustomer>) progressListener)
                .build();
    }

    // ========== 会社移行設定 ==========

    /**
     * 会社データTSVファイルを読み込むReaderを生成します。
     *
     * <p>
     * このReaderは以下の処理を実行します：
     * </p>
     * <ul>
     * <li>TSVファイル（タブ区切り）の読み込み</li>
     * <li>1行目（ヘッダー行）のスキップ</li>
     * <li>各フィールドをOldCompanyDtoにマッピング</li>
     * <li>数値フィールドの文字列から適切な型（Integer, Long）への変換</li>
     * <li>日付の文字列からLocalDateへの変換</li>
     * </ul>
     *
     * @return 会社データを読み込むFlatFileItemReader
     */
    @Bean
    public FlatFileItemReader<OldCompanyDto> oldCompanyTsvReader() {
        return new FlatFileItemReaderBuilder<OldCompanyDto>()
                .name("oldCompanyTsvReader")
                .resource(companyInputResource)
                .linesToSkip(1)
                .delimited()
                .delimiter("\t")
                .names("companyCode", "companyName", "representativeName", "industryType",
                        "employeeCount", "capital", "establishedDate", "address",
                        "postalCode", "phone", "email", "status")
                .fieldSetMapper(fieldSet -> {
                    OldCompanyDto dto = new OldCompanyDto();
                    dto.setCompanyCode(fieldSet.readString("companyCode"));
                    dto.setCompanyName(fieldSet.readString("companyName"));
                    dto.setRepresentativeName(fieldSet.readString("representativeName"));

                    String industryTypeStr = fieldSet.readString("industryType");
                    if (industryTypeStr != null && !industryTypeStr.isEmpty()) {
                        dto.setIndustryType(Integer.parseInt(industryTypeStr));
                    }

                    String employeeCountStr = fieldSet.readString("employeeCount");
                    if (employeeCountStr != null && !employeeCountStr.isEmpty()) {
                        dto.setEmployeeCount(Integer.parseInt(employeeCountStr));
                    }

                    String capitalStr = fieldSet.readString("capital");
                    if (capitalStr != null && !capitalStr.isEmpty()) {
                        dto.setCapital(Long.parseLong(capitalStr));
                    }

                    String establishedDateStr = fieldSet.readString("establishedDate");
                    if (establishedDateStr != null && !establishedDateStr.isEmpty()) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        dto.setEstablishedDate(LocalDate.parse(establishedDateStr, formatter));
                    }

                    dto.setAddress(fieldSet.readString("address"));
                    dto.setPostalCode(fieldSet.readString("postalCode"));
                    dto.setPhone(fieldSet.readString("phone"));
                    dto.setEmail(fieldSet.readString("email"));
                    dto.setStatus(fieldSet.readString("status"));

                    return dto;
                })
                .build();
    }

    /**
     * 新会社データをデータベースに書き込むWriterを生成します。
     *
     * <p>
     * このWriterは、JPAのEntityManagerを使用してデータベースに
     * エンティティを永続化します。
     * </p>
     *
     * @param newEntityManagerFactory 新データベース用のEntityManagerFactory
     * @return 会社データを書き込むJpaItemWriter
     */
    @Bean
    @StepScope
    public ItemWriter<NewCompany> newCompanyWriter(
            @Qualifier("newEntityManagerFactory") EntityManagerFactory newEntityManagerFactory,
            @Value("#{jobParameters['upsertEnabled']}") String upsertEnabled) {

        if ("true".equalsIgnoreCase(upsertEnabled)) {
            // SharedEntityManagerCreator creates a transactional EntityManager proxy
            EntityManager sharedEntityManager = org.springframework.orm.jpa.SharedEntityManagerCreator
                    .createSharedEntityManager(newEntityManagerFactory);
            return new UpsertItemWriter<>(sharedEntityManager);
        }

        return new JpaItemWriterBuilder<NewCompany>()
                .entityManagerFactory(newEntityManagerFactory)
                .build();
    }

    /**
     * 会社データ移行ステップを生成します。
     *
     * <p>
     * このステップは、以下の処理をチャンク単位（100件ずつ）で実行します：
     * </p>
     * <ol>
     * <li>TSVファイルから会社データを読み込み（Reader）</li>
     * <li>旧形式から新形式にデータを変換（Processor）</li>
     * <li>新データベースに書き込み（Writer）</li>
     * </ol>
     *
     * @param jobRepository       バッチジョブのメタデータを管理するリポジトリ
     * @param transactionManager  トランザクション管理用マネージャー
     * @param oldCompanyTsvReader 会社データTSVリーダー
     * @param companyProcessor    会社データ変換プロセッサ
     * @param newCompanyWriter    会社データライター
     * @return 会社データ移行ステップ
     */
    @Bean
    public Step companyMigrationStep(JobRepository jobRepository,
            @Qualifier("newTransactionManager") PlatformTransactionManager transactionManager,
            FlatFileItemReader<OldCompanyDto> oldCompanyTsvReader,
            @Qualifier("effectiveCompanyProcessor") ItemProcessor<OldCompanyDto, NewCompany> companyProcessor,
            ItemWriter<NewCompany> newCompanyWriter,
            com.example.batch.listener.CustomSkipListener<OldCompanyDto, NewCompany> skipListener,
            com.example.batch.listener.ProgressListener<OldCompanyDto, NewCompany> progressListener) {
        return new StepBuilder("companyMigrationStep", jobRepository)
                .<OldCompanyDto, NewCompany>chunk(chunkSize, transactionManager)
                .reader(oldCompanyTsvReader)
                .processor(companyProcessor)
                .writer(newCompanyWriter)
                // エラーハンドリング設定
                .faultTolerant()
                .skip(Exception.class) // 全ての例外をスキップ対象に
                .skipLimit(skipLimit) // application.ymlで設定可能
                .retry(org.springframework.dao.DeadlockLoserDataAccessException.class)
                .retry(org.springframework.dao.TransientDataAccessException.class)
                .retryLimit(3) // 最大3回リトライ
                .listener(skipListener)
                // 進捗監視
                .listener((ChunkListener) progressListener)
                .listener((ItemReadListener<OldCompanyDto>) progressListener)
                .listener((ItemWriteListener<NewCompany>) progressListener)
                .build();
    }

    // ========== ジョブ定義 ==========

    // ========== デシジョン定義 ==========

    /**
     * ステップを実行するかどうかを決定するDecider。
     * ジョブパラメータ "targets" に対象が含まれている場合のみ CONTINUE を返す。
     */
    public static class MigrationStepDecider implements JobExecutionDecider {
        private final String targetName;

        public MigrationStepDecider(String targetName) {
            this.targetName = targetName;
        }

        @Override
        public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
            String targets = jobExecution.getJobParameters().getString("targets");
            // パラメータがない場合（デフォルト）は全て実行
            if (targets == null || targets.isEmpty() || targets.contains(targetName)) {
                return new FlowExecutionStatus("CONTINUE");
            }
            return new FlowExecutionStatus("SKIP");
        }
    }

    /**
     * データ移行バッチジョブを生成します。
     *
     * <p>
     * このジョブは、ジョブパラメータ 'targets' に基づいてステップを実行します。
     * </p>
     *
     * @return データ移行バッチジョブ
     */
    @Bean
    public Job dataMigrationJob(JobRepository jobRepository,
            Step customerMigrationStep,
            Step companyMigrationStep,
            JobCompletionNotificationListener listener,
            com.example.batch.listener.StatisticsReportListener statisticsReportListener,
            com.example.batch.listener.RollbackListener rollbackListener) {

        MigrationStepDecider customerDecider = new MigrationStepDecider("customer");
        MigrationStepDecider companyDecider = new MigrationStepDecider("company");

        // Customer Flow
        Flow customerFlow = new FlowBuilder<Flow>("customerFlow")
                .start(customerDecider)
                .on("SKIP").end()
                .from(customerDecider)
                .on("CONTINUE").to(customerMigrationStep)
                .build();

        // Company Flow
        Flow companyFlow = new FlowBuilder<Flow>("companyFlow")
                .start(companyDecider)
                .on("SKIP").end()
                .from(companyDecider)
                .on("CONTINUE").to(companyMigrationStep)
                .build();

        return new JobBuilder("dataMigrationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .listener(statisticsReportListener)
                .listener(rollbackListener)
                .start(customerFlow)
                .next(companyFlow)
                .build()
                .build();
    }
}
