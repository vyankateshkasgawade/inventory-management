// export interface RegistrationRequest {
//   name: string;
//   email: string;
//   password: string;
//   otp: string;
//   dob: string; // or Date
//   phone: string;
//   role: string;
//   isActive: boolean;
// }
export interface RegistrationRequest {
  name: string;
  email: string;
  password: string;
  otp: string;
  phone: string;  // Must match backend
  dob: string;    // Must match backend
  role: string;   // Must match backend
}