---
title: Support Policy
---
# Maintenance and Security Policy

## Supported Versions

The following versions are supported with security updates

| Version | Release date       | End of Standard Support | End of Life |
| ------- | ------------------ | ----------------------- | ----------- |
| 5.X     | 2023-03            | 2025-03                 | 2026-03     |
| 4.X     | 2021-03            | 2023-03                 | 2024-03     |
| 3.7.X   | 2022-04            | 2022-09                 | 2023-09     |


Note: After *End of Standard Support* a release will only get security patches.

## Dependencies support

| RODA    | Java          | Apache Tomcat     | Apache Solr             | E-ARK IP           |
| ------- | ------------- | ----------------- | ----------------------- | ------------------ |
| 5.X     | Java 17 (LTS) | Apache Tomcat 9   | Apache Solr 9.X         | E-ARK IP 2.X-S[^3] | 
| 4.X     | Java 8 (LTS)  | Apache Tomcat 8.5 | Apache Solr 8.X[^1]     | E-ARK IP 2.X[^2]   |  
| 3.7.X   | Java 8 (LTS)  | Apache Tomcat 8.5 | Apache Solr 7.7.3 (EOL) | E-ARK IP 1.X       |

[^1]: RODA 4.4 upgraded from Solr 7 to 8
[^2]: RODA 4.2 introduced E-ARK IP 2.X
[^3]: RODA 5 added support for URLs instead of files in E-ARK IP (S from Shallow).

## Reporting a Vulnerability

To report on a vulnerability, please use the github forms for vulnerability reporting:

https://github.com/keeps/roda/security/advisories/new

After our security team assesses the reported vulnerability, you'll be informed if it was accepted and how long it would take for a security fix.
