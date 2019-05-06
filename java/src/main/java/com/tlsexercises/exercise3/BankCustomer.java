package com.tlsexercises.exercise3;

import java.io.*;

/**
 * Example class illustrating the use of serialization in transporting objects across SSL connections.
 *
 * This is purely an example - Banks should implement their own classes!
 *
 */
public class BankCustomer implements Serializable {

    private static final long serialVersionUID = 1001338395385763920L;

    private int id;
    private String name;
    private String emailAddress;
    private int accountNumber;
    private int sortCode;
    private String address1;
    private String address2;
    private String town;
    private String postCode;

    public BankCustomer(int id, String name, String email, int accn, int srtc, String add1, String add2, String town, String pc) {
        this.id = id;
        this.name = name;
        this.emailAddress = email;
        this.accountNumber = accn;
        this.sortCode = srtc;
        this.address1 = add1;
        this.address2 = add2;
        this.town = town;
        this.postCode = pc;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public int getSortCode() {
        return sortCode;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BankCustomer other = (BankCustomer) o;

        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public String toString(){
        StringBuilder srtString = new StringBuilder();
        srtString.append(this.sortCode);
        srtString.insert(4, '-');
        srtString.insert(2,'-');
        return String.format("----------\n%d\n%s\nAccount: %d (%s)\n%s, %s, %s, %s\n----------\n",
                this.id,
                this.name,
                this.accountNumber,
                srtString.toString(),
                this.address1, this.address2, this.town, this.postCode);
    }
}
