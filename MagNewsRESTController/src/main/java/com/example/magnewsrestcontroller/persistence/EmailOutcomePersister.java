package com.example.magnewsrestcontroller.persistence;

import com.example.magnewsrestcontroller.model.EmailOutcome;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EmailOutcomePersister {

    @Insert("insert into emailoutcomes (id, outcome) values(#{id}, #{outcome, typeHandler=org.apache.ibatis.type.EnumOrdinalTypeHandler})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int saveEmailOutcome(EmailOutcome emailOutcome);

    @Update("update emailoutcomes set outcome=#{outcome, typeHandler=org.apache.ibatis.type.EnumOrdinalTypeHandler} where id=#{id}")
    void updateEmailOutcome(EmailOutcome emailOutcome);

    @Select("select * from emailoutcomes")
    List<EmailOutcome> getAllEmailOutcomes();

    @Select("select * from emailoutcomes where id=#{id}")
    EmailOutcome getEmailOutcomeById(int id);
}
