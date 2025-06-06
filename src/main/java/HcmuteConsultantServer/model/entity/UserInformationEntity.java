package HcmuteConsultantServer.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Data
@Builder
@Entity
@Table(name = "user_information")
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class UserInformationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "created_at", updatable = false)
    private LocalDate createdAt;

    @Column(name = "student_code", length = 50, unique = true)
    private String studentCode;

    @Column(name = "school_name", length = 255)
    private String schoolName;

    @Column(name = "firstname", length = 50)
    private String firstName;

    @Column(name = "lastname", length = 50)
    private String lastName;

    @Column(name = "phone", length = 10, unique = true)
    private String phone;

    @Column(name = "avatar_url", length = 900)
    private String avatarUrl;

    @Column(name = "gender", length = 3)
    private String gender;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    @JsonIgnore
    private AddressEntity address;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PostEntity> posts;

    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    @JsonBackReference
    private AccountEntity account;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<RoleAuthEntity> roleAuths;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<ConversationEntity> userConversations;

    @OneToMany(mappedBy = "consultant", cascade = CascadeType.ALL)
    private Set<ConversationEntity> consultantConversations;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<ConsultationScheduleEntity> userConsultations;

    @OneToMany(mappedBy = "consultant", cascade = CascadeType.ALL)
    private Set<ConsultationScheduleEntity> consultantConsultations;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<QuestionEntity> questions;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private Set<CommonQuestionEntity> commonQuestions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<AnswerEntity> answers;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<RatingEntity> ratingUsers;

    @OneToMany(mappedBy = "consultant", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<RatingEntity> ratingConsultants;

    @OneToMany(mappedBy = "userComment", cascade = CascadeType.ALL)
    private List<CommentEntity> comments;

    public String getName() {
        return this.lastName + " " + this.firstName;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInformationEntity that = (UserInformationEntity) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public String toString() {
        return "UserInformationEntity{" +
                "id=" + id +
                ", studentCode='" + studentCode + '\'' +
                ", schoolName='" + schoolName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", gender='" + gender + '\'' +
                ", address=" + (address != null ? address.getId() : null) +  // Only include address ID
                '}';
    }
}
