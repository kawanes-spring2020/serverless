package com.lamda.sns;

    import java.net.URL;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.Calendar;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    import com.amazonaws.services.lambda.runtime.RequestHandler;
    import com.amazonaws.auth.AWSStaticCredentialsProvider;
    import com.amazonaws.auth.BasicAWSCredentials;
    import com.amazonaws.regions.Regions;
    import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
    import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
    import com.amazonaws.services.dynamodbv2.document.DynamoDB;
    import com.amazonaws.services.dynamodbv2.document.Item;
    import com.amazonaws.services.dynamodbv2.document.Table;
    import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
    import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
    import com.amazonaws.services.dynamodbv2.model.PutItemResult;
    import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
    import com.amazonaws.services.dynamodbv2.model.ReturnValue;
    import com.amazonaws.services.lambda.runtime.Context;
    import com.amazonaws.services.lambda.runtime.events.SNSEvent;
    import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
    import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
    import com.amazonaws.services.simpleemail.model.Body;
    import com.amazonaws.services.simpleemail.model.Content;
    import com.amazonaws.services.simpleemail.model.Destination;
    import com.amazonaws.services.simpleemail.model.Message;
    import com.amazonaws.services.simpleemail.model.SendEmailRequest;

    public class App implements RequestHandler<SNSEvent, Object>
    {
        
        static final String FROM = "finalAWS@prod.shubhamkawane.me";
        static String TO = "";
        static final String CONFIGSET = "ConfigSet";
        static final String SUBJECT = "Assignment 10";  
         String HTMLBODY = "";
        static String TEXTBODY = "";
        
            public  Object handleRequest(SNSEvent request, Context context){
                try {
                    BasicAWSCredentials bAWSc = new BasicAWSCredentials("AKIASYMCGX2UCG3IF4Z6", "W1nSdcATQg9wcKgT92uHu6Pjyb6yUcTLO5PLlYeq");
                    AmazonDynamoDB dynamoclient = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1).withCredentials(new AWSStaticCredentialsProvider(bAWSc)).build();
                    
                    long epoch = (System.currentTimeMillis()/1000)+3600;
                    
                    
                    String s1=request.getRecords().get(0).getSNS().getMessage();
                    String replace = s1.replace("[","");
                    String replace1 = replace.replace("]","");
                    System.out.println(replace1);
                    List<String> myList = new ArrayList<String>(Arrays.asList(replace1.split(",")));
                    System.out.println(myList.toString());
                    
                    
                    TO = myList.get(myList.size()-1);
                    
                    
                    GetItemRequest requesttt = new GetItemRequest();
            		requesttt.setTableName("test");
            		requesttt.setConsistentRead(true);
                    Map<String, AttributeValue> keysMap = new HashMap();
                    keysMap.put("username", new AttributeValue(TO));        
                    requesttt.setKey(keysMap);
                    GetItemResult result1 = dynamoclient.getItem(requesttt);
                    System.out.println(result1.getItem());
                       HTMLBODY="";
	                    if(result1.getItem()==null) {
	                    	HTMLBODY+="<h1>Following bills are due:</h1>";
	                        for(int i=0;i<myList.size()-1;i++) {
	                            HTMLBODY+=new URL(myList.get(i));
	                            HTMLBODY+="<br>";
	                        }
	                    	Map<String, AttributeValue> map = new HashMap();
	                        map.put("username", new AttributeValue(TO));
	                        map.put("ttl", new AttributeValue(String.valueOf(epoch)));
		                    PutItemRequest request11 = new PutItemRequest();
		                    request11.setTableName("test");
		                    request11.setItem(map);
		                    PutItemResult result = dynamoclient.putItem(request11);
		                    
		                    AmazonSimpleEmailService client =  AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.US_EAST_1).withCredentials(new AWSStaticCredentialsProvider(bAWSc)).build();
		                   
		                     
		        
		                SendEmailRequest request1 = new SendEmailRequest()
		                          .withDestination(
		                              new Destination().withToAddresses(TO))
		                          .withMessage(new Message()
		                              .withBody(new Body()
		                                  .withHtml(new Content()
		                                      .withCharset("UTF-8").withData(HTMLBODY))
		                                  .withText(new Content()
		                                      .withCharset("UTF-8").withData(TEXTBODY)))
		                              .withSubject(new Content()
		                                  .withCharset("UTF-8").withData(SUBJECT)))
		                          .withSource(FROM);
		                client.sendEmail(request1);
		                System.out.println("Email sent!");
		            context.getLogger().log(request.getRecords().get(0).getSNS().getMessage());
                    }
                }catch(Exception e) {
                    System.out.println("Email not sent");
                }

                   return null;
             }
        
    }
