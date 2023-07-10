# DBeaver Agent

- 产品 id product-id dbeaver-ee
- 产品版本 6.3
- 许可 ID DB-ZS1MPZK4--FVE

## checkLicenseStatus

返回内容 VALID: Ok

## checkCustomerEmail

返回内容 疑似用户 ID

请求的 Header 中有一个字段名为`X-Referrer`,内容为`clientId`,应该就是上述获取的 ID

## generateTrialLicense

返回格式

-- DBeaver EE LICENSE - DB-ZS1MPZT9--FUB
-- Issued at Tue Jan 28 17:04:37 UTC 2020 to Mr Wu //
GcEVPtVH+fzyCX3Jw/b2iDGHIYe20IwwGGzvCaSvgN+SOLyeOfmhTgIXkhhuJsCi7Ov/7Sy2Hpk3
VdHjehLS727GlKOKKKkZ6s9C8bt+Aw4WEhDsivOJpQt5eLUjvDLhZC0ols4R9kIXHRo1KcS5AaHy
8EWhdiuxPOJdHTR01waJUvb4RdH8Ldi2m2CNB93sv1OTMvzoDX1oWUnWGN8mL7K0UU+3ksy06a0O
/AU8wueD1yaXHQp9OML5WmBDZapiuSKoQUH/dPhu6C7XRj1EAiTueNibb9rSfbhlUYKgA/1is4nW
42xwiN3+jzQrBYO1NQIYAlGHxlsJ0+IxqVLHCw==  
请求格式

```xml
<?xml version="1.0" encoding="UTF-8"?>
<request license="utilmate" productId="dbeaver-ee" productVersion="6.3">
	<customer email="宁の邮箱" firstName="XXX" lastName="XXX" company=""></customer>
</request>
```

注意`Content-Type`必须使用`text/xml`

## LicenseKeyProvider

路径 com/dbeaver/ee/runtime/lm/DBeaverEnterpriseLM$LicenseKeyProvider  
类名 com.dbeaver.ee.runtime.lm.DBeaverEnterpriseLM$LicenseKeyProvider
