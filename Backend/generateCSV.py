import pymysql, csv

endpoint = '${DATABASE_URL}'
username = '${DATABASE_USERNAME}'
password = '${DATABASE_PASSWORD2}'
database_name = 'projectDatabase'

# Create the connection
connection = pymysql.connect(host=endpoint, user=username, passwd=password, db=database_name)

def handler():
    # Create the cursor
    cursor = connection.cursor()

    # Execute the statement
    cursor.execute('SELECT * from backup')

    # Get the results
    rows = cursor.fetchall()

    # Export to CSV
    with open("allData.csv", "w") as file:
        for row in rows:
            csv.writer(file).writerow(row)

    # Close the cursor
    cursor.close()

    # Close the connection
    connection.close()

handler()
    