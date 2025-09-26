def getConn():
    return mysql.connector.connect(
    host="localhost",
    user="root",
    password="mysqlaccess",
    database="CricketMatchData"
)