package utils

object DicomCheckerUtils {

    const val MY_AETITLE = "RSI-SCU"
    const val DEST_AETITLE = "STORESCU"

    const val jsonFile = "--ddf3025c-3c3e-40a5-9ed9-92cfa5221f08\n" +
            "Content-Type: application/dicom+xml\n" +
            "Content-Length: 2275\n" +
            "MIME-Version: 1.0\n" +
            "{\n" +
            "  \"00020010\": {\n" +
            "    \"vr\": \"UI\",\n" +
            "    \"Value\": [\n" +
            "      \"1.2.840.10008.1.2.1\"\n" +
            "    ]\n" +
            "  },\n" +
            "  \"00080020\": {\n" +
            "    \"vr\": \"DA\",\n" +
            "    \"Value\": [\n" +
            "      \"20171120\"\n" +
            "    ]\n" +
            "  },\n" +
            "  \"00080030\": {\n" +
            "    \"vr\": \"TM\",\n" +
            "    \"Value\": [\n" +
            "      \"143600\"\n" +
            "    ]\n" +
            "  },\n" +
            "  \"00080050\": {\n" +
            "    \"vr\": \"SH\",\n" +
            "    \"Value\": []\n" +
            "  },\n" +
            "  \"00100010\": {\n" +
            "    \"vr\": \"PN\",\n" +
            "    \"Value\": [\n" +
            "      {\n" +
            "        \"Alphabetic\": \"Dcloud^Anonymized\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"00100020\": {\n" +
            "    \"vr\": \"LO\",\n" +
            "    \"Value\": [\n" +
            "      \"Dcloud.Anonymized\"\n" +
            "    ]\n" +
            "  },\n" +
            "  \"0020000D\": {\n" +
            "    \"vr\": \"UI\",\n" +
            "    \"Value\": [\n" +
            "      \"1.2.392.200036.9123.100.12.12.20352.90171120143006423293530504\"\n" +
            "    ]\n" +
            "  },\n" +
            "  \"00200010\": {\n" +
            "    \"vr\": \"SH\",\n" +
            "    \"Value\": []\n" +
            "  }\n" +
            "}" +
            "\n" +
            "--ddf3025c-3c3e-40a5-9ed9-92cfa5221f08"

}