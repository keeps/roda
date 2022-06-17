# Maintenance and Security Policy

## Supported Versions

The following versions are supported with security updates

| Version | Release date       | End of Standard Support | End of Life |
| ------- | ------------------ | ----------------------- | ----------- |
| 5.X[^1]    | 2022-07            | 2024-07                 | 2025-07     |
| 4.X     | 2021-03            | 2023-03                 | 2024-03     |
| 3.7.X   | 2022-04            | 2022-09                 | 2023-09     |

[^1]: Upcoming release

Note: After *End of Standard Support* a release will only get security patches.

## Dependencies support

| RODA    | Java          | Apache Tomcat     | Apache Solr             | E-ARK IP           |
| ------- | ------------- | ----------------- | ----------------------- | ------------------ |
| 5.X     | Java 11 (LTS) | Apache Tomcat 9   | Apache Solr 9.X         | E-ARK IP 2.X-S[^4] | 
| 4.X     | Java 8 (LTS)  | Apache Tomcat 8.5 | Apache Solr 8.X[^2]     | E-ARK IP 2.X[^3]   |  
| 3.7.X   | Java 8 (LTS)  | Apache Tomcat 8.5 | Apache Solr 7.7.3 (EOL) | E-ARK IP 1.X       |

[^2]: RODA 4.4 upgraded from Solr 7 to 8
[^3]: RODA 4.2 introduced E-ARK IP 2.X
[^4]: RODA 5 added support for URLs instead of files in E-ARK IP (S from Shallow).

## Reporting a Vulnerability

To report on a vulnerability, please send an email to security@keep.pt with the following information:
* The product and its version where the vulnerability was detected (e.g. RODA v3.3.1)
* Details on the vulnerability
* If possible, the steps to detect that the vulnerability is there.

After our security team assesses the reported vulnerability, you'll be informed if it was accepted and how long it would take for a security fix.
