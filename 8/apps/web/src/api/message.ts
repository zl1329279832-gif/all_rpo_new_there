import { get, post, put } from "@/utils/request";
import type { Message, PageParams, PageResult } from "@/types";

export function getMessageList(params: PageParams & { type?: string; isRead?: number }) {
  return get<PageResult<Message>>("/messages", params);
}

export function getUnreadCount() {
  return get<{ count: number }>("/messages/unread/count");
}

export function markAsRead(id: number) {
  return put(`/messages/${id}/read`);
}

export function markAllAsRead() {
  return put("/messages/read-all");
}

export function sendMessage(data: { title: string; content: string; type: string; userIds: number[] }) {
  return post("/messages", data);
}
