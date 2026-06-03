<script setup lang="ts">
import { useInventoryStore } from '../store/useInventoryStore'

const inventoryStore = useInventoryStore()

const emit = defineEmits<{
  close: []
}>()

const statusColors: Record<string, string> = {
  normal: '#00B42A',
  reserved: '#165DFF',
  damaged: '#F53F3F',
}

const statusTexts: Record<string, string> = {
  normal: '正常',
  reserved: '预留',
  damaged: '损坏',
}

const zoneNames: Record<string, string> = {
  inbound: '入库区',
  storage: '存储区',
  outbound: '出库区',
  picking: '拣选区',
}

function handleClose() {
  inventoryStore.selectLocation(null)
  emit('close')
}

function handleOverlayClick(event: MouseEvent) {
  if (event.target === event.currentTarget) {
    handleClose()
  }
}
</script>

<template>
  <Teleport to="body">
    <div v-if="inventoryStore.selectedLocation" class="modal-overlay" @click="handleOverlayClick">
      <div class="modal-container">
        <div class="modal-header">
          <h3 class="modal-title">库位详情</h3>
          <button class="close-btn" @click="handleClose">✕</button>
        </div>

        <div class="modal-body">
          <div class="location-info">
            <div class="info-row">
              <span class="info-label">库位编号</span>
              <span class="info-value highlight">{{ inventoryStore.selectedLocation.id }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">所属区域</span>
              <span class="info-value">{{ zoneNames[inventoryStore.selectedLocation.zone] }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">货架/列/层</span>
              <span class="info-value">
                R{{ inventoryStore.selectedLocation.row }} / 
                {{ inventoryStore.selectedLocation.bay }} / 
                {{ inventoryStore.selectedLocation.level }}
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">最大承重</span>
              <span class="info-value">{{ inventoryStore.selectedLocation.maxWeight }} kg</span>
            </div>
            <div class="info-row">
              <span class="info-label">状态</span>
              <span class="info-value" :class="{ occupied: inventoryStore.selectedLocation.occupied }">
                {{ inventoryStore.selectedLocation.occupied ? '已占用' : '空闲' }}
              </span>
            </div>
          </div>

          <div v-if="inventoryStore.selectedCargo" class="cargo-section">
            <h4 class="section-title">货物信息</h4>
            
            <div class="cargo-info">
              <div class="info-row">
                <span class="info-label">货物编号</span>
                <span class="info-value">{{ inventoryStore.selectedCargo.id }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">SKU</span>
                <span class="info-value">{{ inventoryStore.selectedCargo.sku }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">货物名称</span>
                <span class="info-value">{{ inventoryStore.selectedCargo.name }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">数量</span>
                <span class="info-value">{{ inventoryStore.selectedCargo.quantity }} 件</span>
              </div>
              <div class="info-row">
                <span class="info-label">重量</span>
                <span class="info-value">{{ inventoryStore.selectedCargo.weight }} kg</span>
              </div>
              <div class="info-row">
                <span class="info-label">批次号</span>
                <span class="info-value">{{ inventoryStore.selectedCargo.batchNo }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">入库日期</span>
                <span class="info-value">{{ inventoryStore.selectedCargo.inboundDate }}</span>
              </div>
              <div v-if="inventoryStore.selectedCargo.expiryDate" class="info-row">
                <span class="info-label">有效期至</span>
                <span class="info-value">{{ inventoryStore.selectedCargo.expiryDate }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">状态</span>
                <span class="info-value" :style="{ color: statusColors[inventoryStore.selectedCargo.status] }">
                  ● {{ statusTexts[inventoryStore.selectedCargo.status] }}
                </span>
              </div>
            </div>
          </div>

          <div v-else class="empty-cargo">
            <div class="empty-icon">📦</div>
            <p>该库位当前无货物</p>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn secondary" @click="handleClose">关闭</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped lang="scss">
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-container {
  background: linear-gradient(135deg, #1e1e2e 0%, #2a2a3e 100%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  width: 420px;
  max-width: 90vw;
  max-height: 80vh;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);

  .modal-title {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #fff;
  }

  .close-btn {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    border: none;
    color: #fff;
    font-size: 16px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;

    &:hover {
      background: rgba(255, 255, 255, 0.2);
    }
  }
}

.modal-body {
  padding: 24px;
  max-height: 60vh;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.05);
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 2px;
  }
}

.location-info,
.cargo-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;

  .info-label {
    font-size: 13px;
    color: #86909C;
  }

  .info-value {
    font-size: 13px;
    color: #fff;
    font-weight: 500;

    &.highlight {
      color: #165DFF;
      font-weight: 600;
    }

    &.occupied {
      color: #FF7D00;
    }
  }
}

.cargo-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);

  .section-title {
    margin: 0 0 16px 0;
    font-size: 14px;
    font-weight: 600;
    color: #C9CDD4;
  }
}

.empty-cargo {
  margin-top: 24px;
  padding: 40px 20px;
  text-align: center;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 12px;
  border: 1px dashed rgba(255, 255, 255, 0.1);

  .empty-icon {
    font-size: 48px;
    margin-bottom: 12px;
    opacity: 0.5;
  }

  p {
    margin: 0;
    font-size: 14px;
    color: #86909C;
  }
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;

  &.secondary {
    background: rgba(255, 255, 255, 0.1);
    color: #fff;

    &:hover {
      background: rgba(255, 255, 255, 0.2);
    }
  }

  &.primary {
    background: linear-gradient(135deg, #165DFF, #4080FF);
    color: #fff;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(22, 93, 255, 0.4);
    }
  }
}
</style>
