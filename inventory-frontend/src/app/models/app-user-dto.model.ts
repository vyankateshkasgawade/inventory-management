export interface AppUserDTO {
  userId?: number;
  name: string;
  email: string;
  dob: string; // or Date
  phone: string;
  password?: string;
  role: string;
  isActive?: boolean;
}

