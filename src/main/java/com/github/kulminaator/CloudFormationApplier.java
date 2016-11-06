package com.github.kulminaator;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.*;
import com.amazonaws.services.cloudformation.model.*;
import com.amazonaws.util.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by martin on 6.10.16.
 */
public class CloudFormationApplier {

    public static void main(String[] args) {
        CloudFormationApplier applier = new CloudFormationApplier();
        applier.applyCloudFormation();
    }

    public void applyCloudFormation() {
        AmazonCloudFormationClient client = this.getClient();
        String body = this.getTemplateBody();
        Collection<Parameter> params = new ArrayList<>();
        //params.add(new Parameter().withParameterKey("key").withParameterValue("value"));
        CreateStackRequest request = new CreateStackRequest()
                .withStackName("VPCStack")
                .withTemplateBody(body)
                .withParameters(params)
                .withOnFailure(OnFailure.DELETE)
                .withTags(new Tag().withKey("StackTag").withValue("StackValue"));
        CreateStackResult result = client.createStack(request);
        System.out.println("Started to create the stack " + result.getStackId());
        this.pollAndShowStack(result.getStackId());
        System.out.println("Done!");
    }

    private AmazonCloudFormationClient getClient() {
        ProfileCredentialsProvider provider = new ProfileCredentialsProvider();
        AmazonCloudFormationClient c = new AmazonCloudFormationClient(provider);
        c.setRegion(Region.getRegion(Regions.EU_WEST_1));
        return c;
    }

    private void pollAndShowStack(String stackId) {
        AmazonCloudFormationClient client = this.getClient();
        DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest()
                .withStackName("VPCStack");
        long start = System.currentTimeMillis();
        while (true) {
            DescribeStacksResult result = client.describeStacks(describeStacksRequest);
            String status = result.getStacks().get(0).getStackStatus();
            String name = result.getStacks().get(0).getStackName();
            long now = System.currentTimeMillis();
            System.out.println((now - start) + "ms : Stack " + name + " status " + status);
            this.safeSleep(5000);
            if (!status.equalsIgnoreCase(StackStatus.CREATE_IN_PROGRESS.toString())) {
                System.out.println("Status not create-in-progress anymore, checking results");
                break;
            }
        }
    }

    private void safeSleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public String getTemplateBody() {
        final String templateBody;
        try {
            templateBody = IOUtils.toString(new FileInputStream("VpcTemplate.json"));
            return templateBody;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
