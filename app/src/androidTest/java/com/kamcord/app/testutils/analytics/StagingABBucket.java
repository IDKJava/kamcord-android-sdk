package com.kamcord.app.testutils.analytics;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

/**
 * Created by Mehmet on 6/10/15.
 */
@DynamoDBTable(tableName = "staging_ab_bucket")
public class StagingABBucket {
    private String experimentId;
    private int bucketNumber;
    private String addedAtDate;
    private String lastModifiedDate;
    private String variantId;

    @DynamoDBIndexRangeKey(attributeName = "experiment_id")
    public String getExperimentId() {
        return this.experimentId;
    }

    public void setExperimentId(String Id) {
        this.experimentId = Id;
    }

    @DynamoDBIndexHashKey(attributeName = "bucket_number")
    public int getBucketNumber() {
        return this.bucketNumber;
    }

    public void setBucketNumber(int bNum) {
        this.bucketNumber = bNum;
    }

    @DynamoDBAttribute(attributeName = "added_at")
    public String getAddedAtDate() {
        return this.addedAtDate;
    }

    public void setAddedAtDate(String addedAt) {
        this.addedAtDate = addedAt;
    }

    @DynamoDBAttribute(attributeName = "variant_id")
    public String getVariantId() {
        return this.variantId;
    }

    public void getVariantId(String varId) {
        this.variantId = varId;
    }


}