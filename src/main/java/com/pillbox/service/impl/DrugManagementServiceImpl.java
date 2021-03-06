package com.pillbox.service.impl;

import com.pillbox.dao.DrugManagementDao;
import com.pillbox.dao.MedicineHistoryDao;
import com.pillbox.dao.TimeDoseDao;
import com.pillbox.po.DrugManagement;
import com.pillbox.po.MedicineHistory;
import com.pillbox.po.TimeDose;
import com.pillbox.po.User;
import com.pillbox.service.DrugManagementService;
import com.pillbox.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User:  maktub
 * Date:   16/5/11 下午12:11
 */
@Service
public class DrugManagementServiceImpl implements DrugManagementService {

    @Autowired
    private UserService userService;

    @Autowired
    private TimeDoseDao timeDoseDao;

    @Autowired
    private DrugManagementDao drugDao;

    @Autowired
    private MedicineHistoryDao historyDao;


    @Override
    public DrugManagement saveOrUpdate(Long drugId, String openId, String medicineName, String surplus, String unit,
                               String takeResion, String takeWay, String doctor, String add_remind,
                               String gap, String times_dose_times, String persist, String dose_type) {

        DrugManagement drug = this.drugDao.selectById(drugId);
        if (drug == null) {
            drug = new DrugManagement();
        }

        User user = this.userService.selectByOpenId(openId);

        Set<TimeDose> timeDoses = formatTime(times_dose_times, drug);

        drug.setUser(user);
        drug.setName(medicineName);
        drug.setSurplus(Long.parseLong(surplus));

        drug.setUnit(unit);
        drug.setUnitStr(DrugManagementDao.Unit.getUnitStr(unit));

        drug.setTake_resion(takeResion);

        drug.setTake_way(takeWay);
        drug.setTake_way_str(DrugManagementDao.Takeway.getTakewayStr(takeWay));

        drug.setDoctor(doctor);
        drug.setAdd_remind(add_remind == null || "".equals(add_remind) ? "0" : "1");

        drug.setGap(gap);
        drug.setGapStr(DrugManagementDao.Gap.getGapStr(gap));

        //如果是每日服药
        if ("1".equals(gap)) {
            drug.setPersist(persist);
            drug.setPersistStr(DrugManagementDao.Persist.getPersistStr(persist));
            drug.setEndtime(calEndTime(persist));
        }else {
            drug.setPersist("");
            drug.setPersistStr("");
            drug.setEndtime(calEndTime("0")); //一次性事件
        }

        drug.setTimes_dose(timeDoses);

        drug.setDose_type(dose_type);
        drug.setDose_type_str(DrugManagementDao.DoseType.getDoseTypeStr(dose_type));

        if (drug.getId() == null) this.drugDao.save(drug);
        else this.drugDao.update(drug);

        saveMedicineHistory(timeDoses, drug);

        return drug;
    }

    //当添加新药品时，把当天的服药情况设为待服药
    private void saveMedicineHistory(Set<TimeDose> doseSet, DrugManagement drug) {

        for (TimeDose dose : doseSet) {

            MedicineHistory history = new MedicineHistory();

            history.setUser(drug.getUser());
            history.setDrug(drug);
            history.setStatus("2");
            history.setStatusStr(MedicineHistoryDao.Status.getStatsStr("2"));
            history.setTimeDose(dose);

            this.historyDao.save(history);
        }
    }

    @Override
    public List<DrugManagement> selectByOpenId(String openId) {
        User user = this.userService.selectByOpenId(openId);
        return this.drugDao.selectByOpenId(user);
    }

    @Override
    public DrugManagement selectById(Long id) {
        return this.drugDao.selectById(id);
    }

    @Override
    public void delete(Long drugId) {
        DrugManagement drug = this.drugDao.selectById(drugId);
        if (drug != null) {
            this.historyDao.deleteByDrug(drug); //删除已初始化的待服药的数据
            this.drugDao.delete(drug);
        }
    }

    @Override
    public DrugManagement updateSurplus(Long drugId, String surplus) {
        DrugManagement drug = this.drugDao.selectById(drugId);
        if (drug != null) {
            drug.setSurplus(Long.parseLong(surplus));
            this.drugDao.update(drug);
        }
        return drug;
    }

    private Date calEndTime(String days) {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(days));
        return calendar.getTime();
    }

    private String formatTime(int dose_time) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02d", dose_time / 3600)).append(" : ").append(String.format("%02d", dose_time / 60 % 60));
        return sb.toString();
    }

    private Set<TimeDose> formatTime(String times_dose_times, DrugManagement drug) {


        if (drug.getId() != null) {
            //删除当天的服药记录
            this.historyDao.deleteTodayHistory(drug);
        }

        Set<TimeDose> timeDoses = new LinkedHashSet<TimeDose>();
        String[] dose_time_nums = times_dose_times.split(";");

        for (String dose_time_num : dose_time_nums) {
            try {
                String dose_time = dose_time_num.substring(0, dose_time_num.indexOf("("));
                String dose_num = dose_time_num.substring(dose_time_num.indexOf("(") + 1, dose_time_num.indexOf(")"));
                String[] times = dose_time.split(":");
                Long time = Long.parseLong(times[0]) * 3600 + Long.parseLong(times[1]) * 60;
                Long num = Long.parseLong(dose_num);

                TimeDose timeDose = new TimeDose();
                timeDose.setTime(time);
                timeDose.setTime_str(dose_time);
                timeDose.setNum(num);
                timeDose.setDrug(drug);

                timeDoses.add(timeDose);

            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return timeDoses;
    }

}
