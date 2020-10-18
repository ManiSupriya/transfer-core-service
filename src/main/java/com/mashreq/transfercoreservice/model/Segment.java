package com.mashreq.transfercoreservice.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;


/**
 * The persistent class for the segment database table.
 */
@Table(name = "segment_ms")
@Entity
@ToString
@EqualsAndHashCode(of = "name", callSuper = false)
@Getter
@Setter
public class Segment extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    private String customerCareNumber;
    private String customerCareEmail;
    @Nationalized
    private String emailContactUsHtmlContent;
    private String emailContactUsLinkText;
    private String emailSignOffCompany;
    // TODO
//    private List<UserGroups> userGroups;
}