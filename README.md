# GriggiTests Bugs Report
+ Invalid number entry results in 2 repeated alert boxes.
+ Able to share data to an number who is not registered in `ap_user` database table.
+ On entering large values (10000000000) in the share data feature for a user, it breaks the database, and results in corruption of data in the database (bit overflows and negative numbers).
+ Allowed to share 0 amount of data to a user.
+ Data Aloocated is not incrementing (+ share Data).

## Latest bugs
+ The router fup widget is not adding `incoming` and `outgoing` data from the connection table. It is only adding from the `incoming` column. 
