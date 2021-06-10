package com.migration

enum MigrationResultSignal[+A]:
    case Success(v: A)
    case FailedMigration
    case NoOpMigration 
   

