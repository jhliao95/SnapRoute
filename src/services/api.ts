import axios from 'axios';

const api = axios.create({
  baseURL: '/api'
});

export interface Trip {
  id?: number;
  title: string;
  date: string;
  description: string;
}

export const tripApi = {
  // 获取所有行程
  getAllTrips: async () => {
    const response = await api.get<Trip[]>('/trips');
    return response.data;
  },

  // 创建新行程
  createTrip: async (trip: Omit<Trip, 'id'>) => {
    const response = await api.post<Trip>('/trips', trip);
    return response.data;
  },

  // 更新行程
  updateTrip: async (id: number, trip: Omit<Trip, 'id'>) => {
    const response = await api.put<Trip>(`/trips/${id}`, trip);
    return response.data;
  },

  // 删除行程
  deleteTrip: async (id: number) => {
    await api.delete(`/trips/${id}`);
  }
}; 