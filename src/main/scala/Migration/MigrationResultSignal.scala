package com.migration

enum MigrationResultSignal[+A]:
    case Success(v: A)
    case FailedMigration(msg: String)
    case NoOpMigration 
   

