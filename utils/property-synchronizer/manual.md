| ![European Union / European Regional Development Fund / Investing in your future](../../docs/img/eu_rdf_75_en.png "Documents that are tagged with EU/SF logos must keep the logos until 1.1.2022, if it has not stated otherwise in the documentation. If new documentation is created  using EU/SF resources the logos must be tagged appropriately so that the deadline for logos could be found.") |
| -------------------------: |

# MISP2 Installation and Configuration Guide

Version: 1.0

## Version history <!-- omit in toc -->

 Date       | Version | Description                                                               | Author
 ---------- | ------- | ------------------------------------------------------------------------- | --------------------
 02.09.0222 | 1.0     | Convert from Word to Markdown and translate to English                    | Raido Kaju

## License <!-- omit in toc -->

This document is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License.
To view a copy of this license, visit <https://creativecommons.org/licenses/by-sa/4.0/>

## Table of content <!-- omit in toc -->

* [1 Introduction](#1-introduction)
* [2 Running the tool](#2-running-the-tool)
* [3 Results](#3-results)

## 1 Introduction

The `PropertySynchronizer` is a utility to make it possible to synchronize `.properties` files.

During the synchronization process, the `source` file contents are copied into the `result` file. During the process,
any properties, that have a value defined in the `target` file will have their values replaced with the ones found
in the `target` file. Properties that exist in the `target` file, but don't exist in the `source` file, are
added to the end of the `result` file.

The file format must adhere to the format defined on the following page:
http://docs.oracle.com/cd/E23095_01/Platform.93/ATGProgGuide/html/s0204propertiesfileformat01.html

## 2 Running the tool

The utility operates with the following three files defined as input arguments: `source`, `target` and `result`.

Arguments should be passed in the form `-{argument_name} {argument_value}`, replacing the curly braces placeholders
with names and values defined in the following table:


 Name       | Required | Has a value | Default value | Description
 ---------- | ---------| ------------|---------------|-----------------------------------------
 h          | No       | No          |               | Displays information about the arguments
 v          | No       | No          |               | Displays information about the version
 s          | Yes      | Yes         |               | The source file
 t          | Yes      | Yes         |               | The target file
 r          | Yes      | Yes         |               | The result file
 e          | No       | Yes         | UTF-8         | The encoding used for the files

As an example, the following command would synchronize the `targetFile.properties` with `sourceFile.properties` and
store the results in `resultFile.properties` with the encoding `ISO-8859-1`:

```bash
java -jar propertySynchronizer.jar -s sourceFile.properties -t targetFile.properties -r resultFile.properties -e ISO-8859-1
```

## 3 Results

In case of errors (e.g. incorrect input parameters, non-existing files or incorrectly structured files), an error
message with the relevant information is shown.

In case of success, information about the results are shown.
