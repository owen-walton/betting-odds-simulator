"""
Connects the program to the database and returns the connection
"""
import mysql.connector
from mysql.connector import MySQLConnection

def get_conn():
    return mysql.connector.connect(
    host="localhost",
    user="root",
    password="mysqlaccess",
    database="CricketMatchData"
)