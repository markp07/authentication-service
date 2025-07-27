import React from "react";

interface ProfileProps {
  onClose: () => void;
  onSetup2FA: () => void;
  onChangePassword: () => void;
}

// Dummy user info, replace with real API call if needed
const user = {
  email: "mark@markpost.nl",
  userName: "mark",
  twoFAEnabled: true,
};

export default function Profile({ onClose, onSetup2FA, onChangePassword }: ProfileProps) {
  return (
    <div className="flex flex-col gap-4">
      <h2 className="text-xl font-bold mb-2">Profile</h2>
      <div className="bg-blue-50 dark:bg-gray-800 rounded p-4 mb-2">
        <div><span className="font-semibold">Email:</span> {user.email}</div>
        <div><span className="font-semibold">Username:</span> {user.userName}</div>
        <div><span className="font-semibold">2FA Enabled:</span> {user.twoFAEnabled ? "Yes" : "No"}</div>
      </div>
      <button className="bg-blue-600 text-white rounded px-4 py-2 font-semibold" onClick={onSetup2FA}>
        Enable 2FA
      </button>
      <button className="bg-purple-600 text-white rounded px-4 py-2 font-semibold" onClick={onChangePassword}>
        Change Password
      </button>
      <button className="bg-gray-600 text-white rounded px-4 py-2 font-semibold" onClick={onClose}>
        Close
      </button>
    </div>
  );
}
