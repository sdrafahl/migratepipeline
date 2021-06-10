package com.migration

enum MigrationResult[+A]:
    case ResultSuccess(v: A)
    case DownMigrationRan
    case NoMigrationWasRan

