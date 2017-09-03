package jutikorn.promptpayqr

/**
 * Created by jutikornvarojananulux on 9/4/2017 AD.
 */

/**!
 * promptpay-qr
 * JavaScript library to generate PromptPay QR code
 * <https://github.com/dtinth/promptpay-qr>
 *
 * Refs:
 * - https://www.blognone.com/node/95133
 * - Ehttps://www.emvco.com/emv-technologies/qrcodes/
 * - https://github.com/dtinth/promptpay-qr
 * - https://github.com/kittinan/php-promptpay-qr/
 *
 * @license MIT
 */
class PromptPay() {


    private val ID_PAYLOAD_FORMAT = "00"
    private val ID_POI_METHOD = "01"
    private val ID_MERCHANT_INFORMATION_BOT = "29"
    private val ID_TRANSACTION_CURRENCY = "53"
    private val ID_TRANSACTION_AMOUNT = "54"
    private val ID_COUNTRY_CODE = "58"
    private val ID_CRC = "63"

    private val PAYLOAD_FORMAT_EMV_QRCPS_MERCHANT_PRESENTED_MODE = "01"
    private val POI_METHOD_STATIC = "11"
    private val POI_METHOD_DYNAMIC = "12"
    private val MERCHANT_INFORMATION_TEMPLATE_ID_GUID = "00"
    private val BOT_ID_MERCHANT_PHONE_NUMBER = "01"
    private val BOT_ID_MERCHANT_TAX_ID = "02"
    private val GUID_PROMPTPAY = "A000000677010111"
    private val TRANSACTION_CURRENCY_THB = "764"
    private val COUNTRY_CODE_TH = "TH"


    fun generatePayload(target: String,
                        amount: Double,
                        mulipleUse: Boolean = true): String {

        val targetType = if (isIdPhoneNumber(target)) BOT_ID_MERCHANT_PHONE_NUMBER else BOT_ID_MERCHANT_TAX_ID

        var data = f(ID_PAYLOAD_FORMAT, PAYLOAD_FORMAT_EMV_QRCPS_MERCHANT_PRESENTED_MODE) +
                f(ID_POI_METHOD, if (mulipleUse) POI_METHOD_DYNAMIC else POI_METHOD_STATIC) +
                f(ID_MERCHANT_INFORMATION_BOT, f(MERCHANT_INFORMATION_TEMPLATE_ID_GUID, GUID_PROMPTPAY) + f(targetType, formatTarget(target))) +
                f(ID_COUNTRY_CODE, COUNTRY_CODE_TH) +
                f(ID_TRANSACTION_CURRENCY, TRANSACTION_CURRENCY_THB) +
                f(ID_TRANSACTION_AMOUNT, formatAmount(amount))

        var dataToCrc = data + ID_CRC + "04"
        data += f(ID_CRC, formatCrc(crc16(dataToCrc.toByteArray())))
        return data
    }

    fun f(id: String, value: String): String = id + ("00" + value.length).substring(-2) + value

    fun isIdPhoneNumber(id: String): Boolean = id.length == 10 && id[0] == '0'


    fun sanitizeTarget(id: String): String = id.replace("[^0-9]+", "")

    fun formatTarget(id: String): String =
            ("0000000000000" + sanitizeTarget(id).replace("^0", "66")).substring(-13)

    fun formatAmount(amount: Double): String = amount.toFixed(2)

    fun Double.toFixed(digits: Int) = java.lang.String.format("%.${digits}f", this)

    fun crc16(buffer: ByteArray): Int {
        var crc = 0xFFFF

        for (j in buffer.indices) {
            crc = crc.ushr(8) or (crc shl 8) and 0xffff
            crc = crc xor (buffer[j].toInt() and 0xff)//byte to int, trunc sign
            crc = crc xor (crc and 0xff shr 4)
            crc = crc xor (crc shl 12 and 0xffff)
            crc = crc xor (crc and 0xFF shl 5 and 0xffff)
        }
        crc = crc and 0xffff
        return crc
    }

    fun formatCrc(crcValue: Int): String =
            ("0000" + crcValue.toString(16).toUpperCase()).substring(-4)

}