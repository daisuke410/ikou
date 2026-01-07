package com.example.batch.processor;

import com.example.batch.entity.newdb.NewCompany;
import com.example.batch.entity.newdb.NewCustomer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * テスト環境用の機密データマスク処理プロセッサ。
 *
 * <p>このプロセッサは、以下の機密情報をマスクします：</p>
 * <ul>
 *   <li>メールアドレス: 一部を***に置換（例: test@example.com → te***@example.com）</li>
 *   <li>電話番号: 中間部分を***に置換（例: 03-1234-5678 → 03-***-5678）</li>
 *   <li>住所: 番地以降を***に置換（例: 東京都渋谷区1-2-3 → 東京都渋谷区***）</li>
 *   <li>郵便番号: 下4桁を***に置換（例: 123-4567 → 123-****）</li>
 * </ul>
 *
 * <p>このプロセッサは、{@code batch.masking.enabled=true}が設定されている場合のみ有効化されます。</p>
 * <p>本番環境では{@code batch.masking.enabled=false}または未設定にすることで無効化できます。</p>
 *
 * @param <T> 処理対象のエンティティ型（NewCustomer または NewCompany）
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 */
@Component
@ConditionalOnProperty(name = "batch.masking.enabled", havingValue = "true")
public class DataMaskingProcessor<T> implements ItemProcessor<T, T> {

    private static final Logger logger = LoggerFactory.getLogger(DataMaskingProcessor.class);

    @Value("${batch.masking.mask-email:true}")
    private boolean maskEmail;

    @Value("${batch.masking.mask-phone:true}")
    private boolean maskPhone;

    @Value("${batch.masking.mask-address:true}")
    private boolean maskAddress;

    @Value("${batch.masking.mask-postal-code:true}")
    private boolean maskPostalCode;

    /**
     * データマスク処理を実行します。
     *
     * @param item 処理対象のエンティティ
     * @return マスク処理後のエンティティ
     * @throws Exception 処理中のエラー
     */
    @Override
    public T process(T item) throws Exception {
        if (item instanceof NewCustomer) {
            return (T) maskCustomerData((NewCustomer) item);
        } else if (item instanceof NewCompany) {
            return (T) maskCompanyData((NewCompany) item);
        }
        return item;
    }

    /**
     * 顧客データのマスク処理を実行します。
     *
     * @param customer 顧客エンティティ
     * @return マスク処理後の顧客エンティティ
     */
    private NewCustomer maskCustomerData(NewCustomer customer) {
        if (maskEmail && customer.getEmailAddress() != null) {
            customer.setEmailAddress(maskEmailAddress(customer.getEmailAddress()));
        }

        if (maskPhone && customer.getPhoneNumber() != null) {
            customer.setPhoneNumber(maskPhoneNumber(customer.getPhoneNumber()));
        }

        if (maskAddress && customer.getFullAddress() != null) {
            customer.setFullAddress(maskAddressString(customer.getFullAddress()));
        }

        if (maskPostalCode && customer.getZipCode() != null) {
            customer.setZipCode(maskPostalCodeString(customer.getZipCode()));
        }

        logger.debug("【マスク処理】顧客データをマスクしました: {}", customer.getCustomerId());
        return customer;
    }

    /**
     * 会社データのマスク処理を実行します。
     *
     * @param company 会社エンティティ
     * @return マスク処理後の会社エンティティ
     */
    private NewCompany maskCompanyData(NewCompany company) {
        if (maskEmail && company.getContactEmail() != null) {
            company.setContactEmail(maskEmailAddress(company.getContactEmail()));
        }

        if (maskPhone && company.getContactPhone() != null) {
            company.setContactPhone(maskPhoneNumber(company.getContactPhone()));
        }

        if (maskAddress && company.getOfficeAddress() != null) {
            company.setOfficeAddress(maskAddressString(company.getOfficeAddress()));
        }

        if (maskPostalCode && company.getZipCode() != null) {
            company.setZipCode(maskPostalCodeString(company.getZipCode()));
        }

        logger.debug("【マスク処理】会社データをマスクしました: {}", company.getCompanyId());
        return company;
    }

    /**
     * メールアドレスをマスクします。
     * 例: test@example.com → te***@example.com
     *
     * @param email 元のメールアドレス
     * @return マスク後のメールアドレス
     */
    private String maskEmailAddress(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() <= 2) {
            return "***@" + domain;
        }

        String masked = localPart.substring(0, 2) + "***";
        return masked + "@" + domain;
    }

    /**
     * 電話番号をマスクします。
     * 例: 03-1234-5678 → 03-***-5678
     *     0312345678 → 03***5678
     *
     * @param phone 元の電話番号
     * @return マスク後の電話番号
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null) {
            return phone;
        }

        // ハイフン区切りの場合
        if (phone.contains("-")) {
            String[] parts = phone.split("-");
            if (parts.length == 3) {
                return parts[0] + "-***-" + parts[2];
            }
        }

        // ハイフンなしの場合（10桁または11桁）
        if (phone.length() >= 6) {
            String prefix = phone.substring(0, 2);
            String suffix = phone.substring(phone.length() - 4);
            return prefix + "***" + suffix;
        }

        return "***";
    }

    /**
     * 住所をマスクします。
     * 例: 東京都渋谷区1-2-3 → 東京都渋谷区***
     *
     * @param address 元の住所
     * @return マスク後の住所
     */
    private String maskAddressString(String address) {
        if (address == null) {
            return address;
        }

        // 都道府県+市区町村までを残す（おおよそ最初の10文字程度）
        // 数字が出現する位置を探して、その前までを残す
        int numberIndex = -1;
        for (int i = 0; i < address.length(); i++) {
            if (Character.isDigit(address.charAt(i))) {
                numberIndex = i;
                break;
            }
        }

        if (numberIndex > 0) {
            // 数字の前の文字列（市区町村名）までを残す
            String prefix = address.substring(0, numberIndex);
            return prefix + "***";
        }

        // 数字が見つからない場合は、前半を残す
        int prefixLength = Math.min(10, address.length() / 2);
        return address.substring(0, prefixLength) + "***";
    }

    /**
     * 郵便番号をマスクします。
     * 例: 123-4567 → 123-****
     *
     * @param postalCode 元の郵便番号
     * @return マスク後の郵便番号
     */
    private String maskPostalCodeString(String postalCode) {
        if (postalCode == null) {
            return postalCode;
        }

        if (postalCode.contains("-")) {
            String[] parts = postalCode.split("-");
            if (parts.length == 2) {
                return parts[0] + "-****";
            }
        }

        // ハイフンなしの場合は前3桁のみ残す
        if (postalCode.length() >= 3) {
            return postalCode.substring(0, 3) + "****";
        }

        return "***";
    }
}
