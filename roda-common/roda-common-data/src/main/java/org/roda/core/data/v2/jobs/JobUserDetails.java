/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.data.v2.jobs;


import java.io.Serializable;

/**
 * @author Shahzod Yusupov <syusupov@keep.pt>
 */
public class JobUserDetails implements Serializable {

    private static final long serialVersionUID = -4032562078756591923L;

    public JobUserDetails(JobUserDetails jobUser) {
        this.username = jobUser.getUsername();
        this.fullname = jobUser.getFullname();
        this.email = jobUser.getEmail();
        this.role = jobUser.getRole();
    }
    public JobUserDetails(){

    }

    private String username;
    private String fullname;
    private String email;
    private String role;


    public String getUsername() {
        return username;
    }

    public String getFullname() {
        return fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setUsername(String cusername) {
        this.username = cusername;
    }

    public void setFullname(String cfullname) {
        this.fullname = cfullname;
    }

    public void setEmail(String cemail) {
        this.email = cemail;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String cRole) {
        this.role = cRole;
    }

    @Override
    public String toString() {
        return "CentralUser{" +
                "username='" + username + '\'' +
                ", fullname='" + fullname + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }

}
