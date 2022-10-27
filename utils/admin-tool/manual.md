| ![European Union / European Regional Development Fund / Investing in your future](../../docs/img/eu_rdf_75_en.png "Documents that are tagged with EU/SF logos must keep the logos until 1.1.2022, if it has not stated otherwise in the documentation. If new documentation is created  using EU/SF resources the logos must be tagged appropriately so that the deadline for logos could be found.") |
| -------------------------: |

# MISP2 Admin Tool Manual

Version: 1.0

## Version history <!-- omit in toc -->

 Date       | Version | Description                                                               | Author
 ---------- | ------- | ------------------------------------------------------------------------- | --------------------
 07.09.2022 | 1.0     | Migrate from the installation manual to this separate manual              | Raido Kaju

## License <!-- omit in toc -->

This document is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
To view a copy of this license, visit <https://creativecommons.org/licenses/by-sa/4.0/>

## Table of content <!-- omit in toc -->

* [1 Introduction](#1-introduction)
* [2 Running the tool](#2-running-the-tool)

## 1 Introduction

This is a tool for administrating the administrator accounts of the MISP2
application.

## 2 Running the tool

This tool is launched from the command line as follows:

```bash
/usr/xtee/app/admintool.sh
```

The list of existing administrator accounts is displayed by default.

Add the `-add` parameter to the command line to add an administrator account:

```bash
/usr/xtee/app/admintool.sh -add
```

Add the `-delete` parameter to the command line to delete the administrator
account:

```bash
/usr/xtee/app/admintool.sh -delete
```
