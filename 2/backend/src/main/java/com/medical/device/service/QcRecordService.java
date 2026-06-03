package com.medical.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.device.common.PageResult;
import com.medical.device.entity.Device;
import com.medical.device.entity.QcRecord;
import com.medical.device.exception.BusinessException;
import com.medical.device.mapper.DeviceMapper;
import com.medical.device.mapper.QcRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QcRecordService {

    private final QcRecordMapper qcRecordMapper;
    private final DeviceMapper deviceMapper;

    public PageResult<QcRecord> listRecords(int pageNum, int pageSize, String keyword,
                                            Integer qcResult, String qcType, Long deviceId,
                                            LocalDate startDate, LocalDate endDate) {
        Page<QcRecord> page = new Page<>(pageNum, pageSize);
        IPage<QcRecord> result = qcRecordMapper.selectPageWithDevice(
                page, keyword, qcResult, qcType, deviceId, startDate, endDate);
        return PageResult.of(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }

    public QcRecord getRecord(Long id) {
        QcRecord record = qcRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("质控记录不存在");
        }
        return record;
    }

    @Transactional(rollbackFor = Exception.class)
    public QcRecord createRecord(QcRecord record) {
        Device device = deviceMapper.selectById(record.getDeviceId());
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        if (record.getQcDate() == null) {
            record.setQcDate(LocalDate.now());
        }

        qcRecordMapper.insert(record);
        updateDeviceQcStatus(record.getDeviceId());

        return record;
    }

    @Transactional(rollbackFor = Exception.class)
    public QcRecord updateRecord(QcRecord record) {
        QcRecord existing = qcRecordMapper.selectById(record.getId());
        if (existing == null) {
            throw new BusinessException("质控记录不存在");
        }

        if (record.getDeviceId() != null && !record.getDeviceId().equals(existing.getDeviceId())) {
            Device device = deviceMapper.selectById(record.getDeviceId());
            if (device == null) {
                throw new BusinessException("设备不存在");
            }
        }

        qcRecordMapper.updateById(record);

        Long deviceId = record.getDeviceId() != null ? record.getDeviceId() : existing.getDeviceId();
        updateDeviceQcStatus(deviceId);
        if (!deviceId.equals(existing.getDeviceId())) {
            updateDeviceQcStatus(existing.getDeviceId());
        }

        return record;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRecord(Long id) {
        QcRecord record = qcRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("质控记录不存在");
        }

        qcRecordMapper.deleteById(id);
        updateDeviceQcStatus(record.getDeviceId());
    }

    public List<QcRecord> listByDeviceId(Long deviceId) {
        LambdaQueryWrapper<QcRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QcRecord::getDeviceId, deviceId);
        wrapper.orderByDesc(QcRecord::getQcDate, QcRecord::getId);
        return qcRecordMapper.selectList(wrapper);
    }

    public Map<String, Object> getStatistics() {
        return Map.of(
            "resultStats", qcRecordMapper.countByResult(),
            "totalRecords", qcRecordMapper.selectCount(new LambdaQueryWrapper<>())
        );
    }

    private void updateDeviceQcStatus(Long deviceId) {
        LambdaQueryWrapper<QcRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QcRecord::getDeviceId, deviceId);
        wrapper.orderByDesc(QcRecord::getQcDate, QcRecord::getId);
        wrapper.last("LIMIT 1");
        QcRecord latestRecord = qcRecordMapper.selectOne(wrapper);

        Device device = deviceMapper.selectById(deviceId);
        if (device != null) {
            if (latestRecord != null) {
                device.setQcStatus(latestRecord.getQcResult());
            } else {
                device.setQcStatus(null);
            }
            deviceMapper.updateById(device);
        }
    }
}
