import json
import boto3
import traceback

def lambda_handler(event, context):
    
    try:
        # Create an instance of boto3 DynamoDB client to connect to the source and destination DynamoDB tables
        dynamodb = boto3.client('dynamodb')
        print('lambda start')
        print(event)
        
        # Define the source and destination DynamoDB table names
        destination_table_name1 = 'SWIPE_LIKE_QUERY'
    
        for record in event['Records'] :
            ddb = record['dynamodb']
            print(record['eventName'])
            if (record['eventName'] == 'INSERT') or (record['eventName'] == 'MODIFY') :
                response = dynamodb.put_item(TableName=destination_table_name1, Item=ddb['NewImage'])
                print('Lambda completed like table successfully!')

    except Exception:
        print('lambda fail')



