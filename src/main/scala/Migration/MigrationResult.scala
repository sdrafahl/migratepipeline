package com.migration

enum MigrationResult[+A]:
    case ResultSuccess(v: A, m: String)
    case DownMigrationRan
    case NoMigrationWasRan

