package com.example.batch.validator;

import com.example.batch.dto.OldCustomerDto;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 顧客データのバリデーションを行うクラス。
 *
 * <p>TSVファイルから読み込んだ顧客データに対して、
 * 以下のビジネスルールチェックを実行します：</p>
 * <ul>
 *   <li>必須項目チェック（顧客コード、顧客名）</li>
 *   <li>メールアドレス形式チェック</li>
 *   <li>電話番号形式チェック</li>
 *   <li>郵便番号形式チェック</li>
 *   <li>性別コード範囲チェック（1または2）</li>
 * </ul>
 *
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 */
@Component
public class CustomerValidator implements Validator<OldCustomerDto> {

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
     * 顧客データのバリデーションを実行します。
     *
     * @param customer バリデーション対象の顧客データ
     * @throws ValidationException バリデーションエラーが発生した場合
     */
    @Override
    public void validate(OldCustomerDto customer) throws ValidationException {
        List<String> errors = new ArrayList<>();

        // 必須項目チェック
        if (customer.getCustomerCode() == null || customer.getCustomerCode().trim().isEmpty()) {
            errors.add("顧客コードは必須です");
        }

        if (customer.getCustomerName() == null || customer.getCustomerName().trim().isEmpty()) {
            errors.add("顧客名は必須です");
        }

        // メールアドレス形式チェック
        if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(customer.getEmail()).matches()) {
                errors.add("メールアドレスの形式が不正です: " + customer.getEmail());
            }
        }

        // 電話番号形式チェック
        if (customer.getPhone() != null && !customer.getPhone().isEmpty()) {
            if (!PHONE_PATTERN.matcher(customer.getPhone()).matches()) {
                errors.add("電話番号の形式が不正です: " + customer.getPhone());
            }
        }

        // 郵便番号形式チェック
        if (customer.getPostalCode() != null && !customer.getPostalCode().isEmpty()) {
            if (!ZIP_PATTERN.matcher(customer.getPostalCode()).matches()) {
                errors.add("郵便番号の形式が不正です: " + customer.getPostalCode());
            }
        }

        // 性別コード範囲チェック
        if (customer.getGenderCode() != null) {
            if (customer.getGenderCode() < 1 || customer.getGenderCode() > 2) {
                errors.add("性別コードは1（男性）または2（女性）である必要があります: " + customer.getGenderCode());
            }
        }

        // エラーがある場合は例外をスロー
        if (!errors.isEmpty()) {
            throw new ValidationException("顧客データバリデーションエラー [" +
                customer.getCustomerCode() + "]: " + String.join(", ", errors));
        }
    }
}
