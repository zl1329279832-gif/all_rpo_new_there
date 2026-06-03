package com.medical.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.device.common.PageResult;
import com.medical.device.entity.CalibrationRecord;
import com.medical.device.entity.Device;
import com.medical.device.exception.BusinessException;
import com.medical.device.mapper.CalibrationRecordMapper;
import com.medical.device.mapper.DeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalibrationRecordService {

    private final CalibrationRecordMapper calibrationRecordMapper;
    private final DeviceMapper deviceMapper;

    public PageResult<CalibrationRecord> listRecords(int pageNum, int pageSize, String keyword,
                                                     Integer calibrationResult, Long deviceId,
                                                     LocalDate startDate, LocalDate endDate) {
        Page<CalibrationRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CalibrationRecord> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(CalibrationRecord::getCalibrationPerson, keyword)
                    .or().like(CalibrationRecord::getCalibrationAgency, keyword)
                    .or().like(CalibrationRecord::getCertificateNumber, keyword)
                    .or().like(CalibrationRecord::getDescription, keyword));
        }
        if (calibrationResult != null) {
            wrapper.eq(CalibrationRecord::getCalibrationResult, calibrationResult);
        }
        if (deviceId != null) {
            wrapper.eq(CalibrationRecord::getDeviceId, deviceId);
        }
        if (startDate != null) {
            wrapper.ge(CalibrationRecord::getCalibrationDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(CalibrationRecord::getCalibrationDate, endDate);
        }

        wrapper.orderByDesc(CalibrationRecord::getCalibrationDate, CalibrationRecord::getId);
        IPage<CalibrationRecord> result = calibrationRecordMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }

    public CalibrationRecord getRecord(Long id) {
        CalibrationRecord record = calibrationRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("校准记录不存在");
        }
        return record;
    }

    @Transactional(rollbackFor = Exception.class)
    public CalibrationRecord createRecord(CalibrationRecord record) {
        Device device = deviceMapper.selectById(record.getDeviceId());
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        if (record.getCalibrationDate() == null) {
            record.setCalibrationDate(LocalDate.now());
        }

        calibrationRecordMapper.insert(record);
        return record;
    }

    @Transactional(rollbackFor = Exception.class)
    public CalibrationRecord updateRecord(CalibrationRecord record) {
        CalibrationRecord existing = calibrationRecordMapper.selectById(record.getId());
        if (existing == null) {
            throw new BusinessException("校准记录不存在");
        }

        if (record.getDeviceId() != null && !record.getDeviceId().equals(existing.getDeviceId())) {
            Device device = deviceMapper.selectById(record.getDeviceId());
            if (device == null) {
                throw new BusinessException("设备不存在");
            }
        }

        calibrationRecordMapper.updateById(record);
        return record;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRecord(Long id) {
        CalibrationRecord record = calibrationRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("校准记录不存在");
        }

        calibrationRecordMapper.deleteById(id);
    }

    public List<CalibrationRecord> listByDeviceId(Long deviceId) {
        LambdaQueryWrapper<CalibrationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CalibrationRecord::getDeviceId, deviceId);
        wrapper.orderByDesc(CalibrationRecord::getCalibrationDate, CalibrationRecord::getId);
        return calibrationRecordMapper.selectList(wrapper);
    }
}
