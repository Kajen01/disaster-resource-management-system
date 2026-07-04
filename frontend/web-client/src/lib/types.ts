export type Role = "ADMIN" | "SHELTER_MANAGER" | "DONOR";

export type UserStatus = "PENDING_APPROVAL" | "ACTIVE" | "INACTIVE" | "SUSPENDED";

export type AuthResponse = {
  userId: number;
  fullName: string;
  email: string;
  username: string;
  role: Role;
  status: UserStatus;
  token: string;
};

export type RegistrationResponse = {
  userId: number;
  fullName: string;
  email: string;
  username: string;
  role: Role;
  status: UserStatus;
  approved: boolean;
  token: string | null;
  message: string;
};

export type Shelter = {
  id: number;
  name: string;
  district: string;
  addressLine1: string;
  addressLine2?: string;
  contactName: string;
  contactPhone: string;
  managerUserId: number;
  latitude?: number;
  longitude?: number;
  capacity: number;
  occupancy: number;
  availableCapacity: number;
  status: "ACTIVE" | "INACTIVE";
};

export type UserResponse = {
  id: number;
  fullName: string;
  email: string;
  username: string;
  role: Role;
  status: UserStatus;
  createdAt: string;
};

export type ResourceBatch = {
  id: number;
  shelterId: number;
  donorEmail?: string;
  resourceType: string;
  resourceName: string;
  unit: string;
  quantityReceived: number;
  quantityAvailable: number;
  expiryDate?: string;
  receivedAt: string;
  sourceDonationRef: string;
};

export type ShortageRequest = {
  id: number;
  shelterId: number;
  resourceType: string;
  resourceName: string;
  unit: string;
  requiredQuantity: number;
  shortageQuantity: number;
  justification?: string;
  status: string;
  createdAt: string;
};

export type Transfer = {
  transferId: number;
  shortageRequestId: number;
  sourceShelterId: number;
  targetShelterId: number;
  reservationId: number;
  sourceBatchId: number;
  donationRef: string;
  resourceType: string;
  resourceName: string;
  unit: string;
  quantity: number;
  status: string;
};

export type TransparencyView = {
  donationRef: string;
  resourceType: string;
  resourceName: string;
  sourceShelterId: number;
  destinationShelterId: number;
  quantity: number;
  timeline: Array<{
    eventType: string;
    details: string;
    occurredAt: string;
  }>;
};

export type DonationHistory = {
  donorEmail: string;
  totalBatches: number;
  batches: ResourceBatch[];
};

export type DonationTraceSummary = {
  transferId: number;
  donationRef: string;
  sourceShelterId: number;
  destinationShelterId: number;
  resourceType: string;
  resourceName: string;
  quantity: number;
  recordedAt: string;
};

export type ServiceHealth = {
  serviceName: string;
  status: string;
  reachable: boolean;
};
