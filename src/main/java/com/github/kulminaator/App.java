package com.github.kulminaator;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello aws user!" );
        System.out.println(System.getenv("AWS_PROFILE"));
        App a = new App();
        Image image = a.findAmi();
        //Instance instance = a.buildMachine(image);
        //a.createImage(instance);
        // release resources
        //instance.setState(new InstanceState().withName(InstanceStateName.Terminated));
    }

    private Instance buildMachine(Image img) {
        AmazonEC2Client client = getClient();
        RunInstancesRequest request = new RunInstancesRequest()
                .withImageId(img.getImageId())
                .withKeyName("aws_isiklik")
                .withMinCount(1)
                .withMaxCount(1)
                .withInstanceType("t2.micro");
        RunInstancesResult response = client.runInstances(request);
        Instance instance = response.getReservation().getInstances().get(0);
        System.out.println("Created instance " + instance.getInstanceId());
        while (true) {
            this.safeSleep();
            InstanceStateName stateName = InstanceStateName.fromValue(instance.getState().getName());
            System.out.println("State is " + stateName);
            if (stateName == InstanceStateName.Running) {
                break;
            }
        }
        return instance;
    }

    private void safeSleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createImage(Instance instance) {
        CreateImageRequest request = new CreateImageRequest()
                .withInstanceId(instance.getInstanceId());
        CreateImageResult result = this.getClient().createImage(request);
        result.getImageId();
    }

    public AmazonEC2Client getClient () {
        ProfileCredentialsProvider provider = new ProfileCredentialsProvider();
        AmazonEC2Client client = new AmazonEC2Client(provider);
        client.setRegion(Region.getRegion(Regions.EU_WEST_1));
        return client;
    }

    public Image findAmi() {
        AmazonEC2Client client = getClient();
        DescribeImagesRequest request = new DescribeImagesRequest();
        request.setImageIds(Arrays.asList("ami-844e0bf7"));
        request.setOwners(Arrays.asList("099720109477"));
        DescribeImagesResult result = client.describeImages(request);
        List<Image> images = result.getImages();

        Image first = images.get(0);
        System.out.println("Found number of images : " + images.size());
        System.out.println("First image was " + first.getImageId());

        return first;
    }
}
