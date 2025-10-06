/*
 * DBeaver License 生成工具
 * 命令行工具类，用于生成 DBeaver 或 CloudBeaver 的 license 文件。
 * 与 Java Agent 拦截流程无直接关联。
 */

package com.dbeaver.agent;

import com.dbeaver.lm.api.LMEncryption;
import com.dbeaver.lm.api.LMLicense;
import com.dbeaver.lm.api.LMLicenseType;
import com.dbeaver.lm.api.LMProduct;
import com.dbeaver.lm.api.LMProductType;
import com.dbeaver.lm.api.LMUtils;
import com.dbeaver.agent.utils.MyCryptKey;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.lang.reflect.Field;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.Callable;

@Command(name = "gen-license", mixinStandardHelpOptions = true, version = "gen-license 1.0",
        description = "Generate DBeaver license")
public class License
        implements Callable<Integer>
{
    @Option(names = {"-h", "--help"}, usageHelp = true)
    private boolean helpRequested = false;

    @Option(names = {"-p", "--product"}, defaultValue = "dbeaver", description = "Product name, you can choose dbeaver or cloudbeaver, default is ${DEFAULT-VALUE}")
    private String productName;

    @Option(names = {"-t",
            "--type"}, defaultValue = "ue", description = "License type, you can choose Lite version(le),  Enterprise version(ee) or Ultimate version(ue) default is ${DEFAULT-VALUE}")
    private String licenseType;

    @Option(names = {"-v", "--version"}, defaultValue = "25", description = "Product version, default is 25")
    private int productVersion;

    private void genLicense()
            throws Exception
    {
        MyCryptKey myCryptKey = new MyCryptKey();
        PrivateKey privateKey = (PrivateKey) myCryptKey.getPrivateKey();
        // 需要注意的是,这里 id 是不一样的 终极版叫 dbeaver-ue
        String productId = productName + "-" + licenseType;
        LMProduct product = new LMProduct(productId,
                "DB",
                "DBeaver Enterprise",
                "DBeaver Enterprise Edition",
                String.valueOf(productVersion),
                LMProductType.DESKTOP,
                new Date(),
                new String[0]);
        String licenseID = LMUtils.generateLicenseId(product);
        String productID = product.getId();
        String productVersion = product.getVersion();
        byte[] licenseData = getLicenseData(licenseID, productID, productVersion);
        byte[] licenseEncrypted = LMEncryption.encrypt(licenseData, privateKey);
        System.out.println("--- " + productId + "(v" + productVersion + ") LICENSE ---");
        System.out.println(Base64.getEncoder().encodeToString(licenseEncrypted));
        System.out.println("--- 请复制上一行 ---");
    }

    private static byte[] getLicenseData(String licenseID, String productID, String productVersion)
            throws NoSuchFieldException, IllegalAccessException
    {
        String ownerID = "080601";
        String ownerCompany = "example.com";
        String ownerName = "owner";
        String ownerEmail = "owner@example.com";
        LMLicense license = new LMLicense(licenseID,
                LMLicenseType.ULTIMATE,
                new Date(),
                new Date(),
                // 这样子就是没有到期
                null,
                0L,
                productID,
                productVersion,
                ownerID,
                ownerCompany,
                ownerName,
                ownerEmail);
        // 反射修改 yearsNumber 用来修改支持年份
        Field yearsNumberField = license.getClass().getDeclaredField("yearsNumber");
        yearsNumberField.setAccessible(true);
        yearsNumberField.set(license, (byte) 127);
        return license.getData();
    }

    public static void main(String[] args)
    {
        int exitCode = new CommandLine(new License()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call()
            throws Exception
    {
        genLicense();
        return 0;
    }
}
