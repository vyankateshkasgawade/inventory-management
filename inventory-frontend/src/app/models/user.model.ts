export interface User {
  id: number;
  username: string;
  role?: string;    // Optional: e.g., 'admin', 'user'
  token?: string
}

