# GriggiTests Bugs Report
+ Invalid number entry results in 2 repeated alert boxes.
+ Able to share data to an number who is not registered in `ap_user` database table.
+ On entering large values (10000000000) in the share data feature for a user, it breaks the database, and results in corruption of data in the database (bit overflows and negative numbers).
