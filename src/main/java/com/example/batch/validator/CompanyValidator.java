package com.example.batch.validator;

import com.example.batch.dto.OldCompanyDto;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 会社データのバリデーションを行うクラス。
 *
 * <p>TSVファイルから読み込んだ会社データに対して、
 * 以下のビジネスルールチェックを実行します：</p>
 * <ul>
 *   <li>必須項目チェック（会社コード、会社名）</li>
 *   <li>メールアドレス形式チェック</li>
 *   <li>電話番号形式チェック</li>
 *   <li>郵便番号形式チェック</li>
 *   <li>業種コード範囲チェック（1-11）</li>
 *   <li>従業員数の妥当性チェック（正の数）</li>
 *   <li>資本金の妥当性チェック（正の数）</li>
 * </ul>
 *
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 */
@Component
public class CompanyValidator implements Validator<OldCompanyDto> {

    // メールアドレスの正規表現パターン
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // 電話番号の正規表現パターン（日本の形式）
    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^(0\\d{1,4}-\\d{1,4}-\\d{4}|0\\d{9,10})$");

    // 郵便番号の正規表現パターン（日本の形式）
    private static final Pattern ZIP_PATTERN =
        Pattern.compile("^\\d{3}-\\d{4}$");

    /**
     * 会社データのバリデーションを実行します。
     *
     * @param company バリデーション対象の会社データ
     * @throws ValidationException バリデーションエラーが発生した場合
     */
    @Override
    public void validate(OldCompanyDto company) throws ValidationException {
        List<String> errors = new ArrayList<>();

        // 必須項目チェック
        if (company.getCompanyCode() == null || company.getCompanyCode().trim().isEmpty()) {
            errors.add("会社コードは必須です");
        }

        if (company.getCompanyName() == null || company.getCompanyName().trim().isEmpty()) {
            errors.add("会社名は必須です");
        }

        // メールアドレス形式チェック
        if (company.getEmail() != null && !company.getEmail().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(company.getEmail()).matches()) {
                errors.add("メールアドレスの形式が不正です: " + company.getEmail());
            }
        }

        // 電話番号形式チェック
        if (company.getPhone() != null && !company.getPhone().isEmpty()) {
            if (!PHONE_PATTERN.matcher(company.getPhone()).matches()) {
                errors.add("電話番号の形式が不正です: " + company.getPhone());
            }
        }

        // 郵便番号形式チェック
        if (company.getPostalCode() != null && !company.getPostalCode().isEmpty()) {
            if (!ZIP_PATTERN.matcher(company.getPostalCode()).matches()) {
                errors.add("郵便番号の形式が不正です: " + company.getPostalCode());
            }
        }

        // 業種コード範囲チェック
        if (company.getIndustryType() != null) {
            if (company.getIndustryType() < 1 || company.getIndustryType() > 11) {
                errors.add("業種コードは1-11の範囲である必要があります: " + company.getIndustryType());
            }
        }

        // 従業員数の妥当性チェック
        if (company.getEmployeeCount() != null && company.getEmployeeCount() < 0) {
            errors.add("従業員数は0以上である必要があります: " + company.getEmployeeCount());
        }

        // 資本金の妥当性チェック
        if (company.getCapital() != null && company.getCapital() < 0) {
            errors.add("資本金は0以上である必要があります: " + company.getCapital());
        }

        // エラーがある場合は例外をスロー
        if (!errors.isEmpty()) {
            throw new ValidationException("会社データバリデーションエラー [" +
                company.getCompanyCode() + "]: " + String.join(", ", errors));
        }
    }
}
