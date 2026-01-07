package com.example.batch.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * 新データベース（PostgreSQL）の接続設定を行うクラス。
 *
 * <p>このクラスは、移行先の新データベースへの接続に必要な以下のBeanを設定します：</p>
 * <ul>
 *   <li>DataSource: HikariCPコネクションプーリング設定</li>
 *   <li>EntityManagerFactory: JPA/Hibernateのエンティティ管理</li>
 *   <li>TransactionManager: トランザクション管理</li>
 * </ul>
 *
 * <p>起動時に schema-new.sql を実行してテーブルを自動作成します。</p>
 *
 * @see com.example.batch.entity.newdb.NewCustomer
 * @see com.example.batch.entity.newdb.NewCompany
 */
@Configuration
public class DatabaseConfiguration {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.hikari.maximum-pool-size}")
    private int maxPoolSize;

    /**
     * 新データベース用のDataSourceを作成します。
     *
     * <p>HikariCPを使用した高性能なコネクションプールを設定します。</p>
     * <p>データベース接続情報はapplication.ymlから読み込まれ、
     * 環境変数で上書き可能です：</p>
     * <ul>
     *   <li>DB_URL: データベースURL</li>
     *   <li>DB_USERNAME: ユーザー名</li>
     *   <li>DB_PASSWORD: パスワード</li>
     *   <li>DB_POOL_SIZE: 最大コネクション数</li>
     * </ul>
     *
     * <p>起動時に schema-new.sql を実行してテーブルを作成します。</p>
     *
     * @return 設定済みのHikariDataSource
     */
    @Bean
    @Primary
    public DataSource newDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setDriverClassName(driverClassName);
        config.setMaximumPoolSize(maxPoolSize);

        DataSource dataSource = new HikariDataSource(config);

        // スキーマ初期化: schema-new.sql を実行
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema-new.sql"));
        populator.execute(dataSource);

        return dataSource;
    }

    /**
     * 新データベース用のEntityManagerFactoryを作成します。
     *
     * <p>JPA/Hibernateのエンティティ管理を行うファクトリーです。</p>
     * <p>com.example.batch.entity.newdb パッケージ配下のエンティティクラスを管理します。</p>
     *
     * @param builder EntityManagerFactoryの構築ヘルパー
     * @param dataSource 新データベース用のDataSource
     * @return 設定済みのEntityManagerFactory
     */
    @Bean(name = { "newEntityManagerFactory", "entityManagerFactory" })
    @Primary
    public LocalContainerEntityManagerFactoryBean newEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("newDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.example.batch.entity.newdb")  // エンティティのスキャン対象パッケージ
                .persistenceUnit("new")
                .build();
    }

    /**
     * 新データベース用のトランザクションマネージャーを作成します。
     *
     * <p>JPAトランザクション管理を提供します。</p>
     * <p>Spring Batchのチャンク処理でコミット・ロールバックを制御します。</p>
     *
     * @param newEntityManagerFactory 新データベース用のEntityManagerFactory
     * @return 設定済みのJpaTransactionManager
     */
    @Bean
    @Primary
    public PlatformTransactionManager newTransactionManager(
            @Qualifier("newEntityManagerFactory") LocalContainerEntityManagerFactoryBean newEntityManagerFactory) {
        return new JpaTransactionManager(newEntityManagerFactory.getObject());
    }
}
