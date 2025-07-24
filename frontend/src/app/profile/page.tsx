"use client";
import React, { useState, useEffect } from "react";
import { IconUser, IconLock } from "@tabler/icons-react";

const USER_API = "http://localhost:12002/v1/user";

export default function ProfilePage() {
  const [userName, setUserName] = useState("");
  const [email, setEmail] = useState("");
  const [createdAt, setCreatedAt] = useState("");
  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [editMode, setEditMode] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchUser() {
      setLoading(true);
      try {
        let res = await fetch(USER_API, {
          credentials: "include"
        });
        if (res.status === 401) {
          // Try to refresh token
          const refreshRes = await fetch("http://localhost:12002/v1/auth/refresh", {
            method: "POST",
            credentials: "include"
          });
          if (refreshRes.status === 401) {
            window.location.href = "/";
            return;
          }
          // Try user fetch again
          res = await fetch(USER_API, {
            credentials: "include"
          });
          if (!res.ok) throw new Error("Failed to fetch user after refresh");
        }
        if (!res.ok) throw new Error("Failed to fetch user");
        const data = await res.json();
        setUserName(data.username);
        setEmail(data.email);
        setCreatedAt(data.createdAt);
      } catch (err) {
        setMessage("Could not fetch user details.");
      } finally {
        setLoading(false);
      }
    }
    fetchUser();
  }, []);

  const handleUserNameUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage(null);
    try {
      const res = await fetch(USER_API, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ username: userName })
      });
      if (!res.ok) throw new Error("Failed to update user name");
      setEditMode(false);
      setMessage("User name updated successfully.");
    } catch (err) {
      setMessage("Could not update user name.");
    }
  };

  const handlePasswordChange = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage(null);
    if (newPassword !== confirmPassword) {
      setMessage("New passwords do not match.");
      return;
    }
    try {
      const res = await fetch("http://localhost:12002/v1/auth/password/change", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
          oldPassword: oldPassword,
          newPassword: newPassword
        })
      });
      if (res.status === 200) {
        setMessage("Password changed successfully.");
        setOldPassword("");
        setNewPassword("");
        setConfirmPassword("");
      } else if (res.status === 400) {
        setMessage("Failed to change password. Please check your old password and password requirements.");
      } else {
        console.err("Error response:", res.status, res.statusText);
        setMessage("Failed to change password.");
      }
    } catch (err) {
      setMessage("Error changing password.");
    }
  };

  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-8 bg-gradient-to-br from-blue-100 via-white to-blue-300 dark:from-gray-900 dark:via-gray-800 dark:to-gray-700">
      <div className="w-full max-w-md bg-white/80 dark:bg-gray-900/80 rounded-lg shadow-2xl p-8 border border-blue-200 dark:border-gray-800 backdrop-blur">
        <h1 className="text-3xl font-bold mb-6 text-blue-700 dark:text-blue-200 text-center">Profile</h1>
        {loading ? (
          <div className="text-center text-blue-700 dark:text-blue-200">Loading...</div>
        ) : (
          <div className="mb-8">
            <div className="mb-4">
              <span className="font-semibold text-blue-700 dark:text-blue-200 flex items-center gap-2"><IconUser size={20} /> User Name:</span>
              {editMode ? (
                <form onSubmit={handleUserNameUpdate} className="flex gap-2 mt-2">
                  <input
                    type="text"
                    className="w-full px-3 py-2 rounded border border-blue-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-black dark:text-white"
                    value={userName}
                    onChange={e => setUserName(e.target.value)}
                  />
                  <button type="submit" className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">Save</button>
                  <button type="button" className="px-4 py-2 bg-gray-400 text-white rounded hover:bg-gray-500" onClick={() => setEditMode(false)}>Cancel</button>
                </form>
              ) : (
                <div className="flex items-center gap-2 mt-2">
                  <span className="text-black dark:text-white">{userName}</span>
                  <button className="ml-2 px-2 py-1 bg-blue-500 text-white rounded hover:bg-blue-600" onClick={() => setEditMode(true)}>Edit</button>
                </div>
              )}
            </div>
            <div className="mb-4">
              <span className="font-semibold text-blue-700 dark:text-blue-200 flex items-center gap-2"><IconUser size={20} /> Email:</span>
              <span className="ml-2 text-black dark:text-white">{email}</span>
            </div>
            <div className="mb-4">
              <span className="font-semibold text-blue-700 dark:text-blue-200 flex items-center gap-2"><IconUser size={20} /> Created At:</span>
              <span className="ml-2 text-black dark:text-white">{createdAt}</span>
            </div>
            {message && <div className="text-red-500 mb-4">{message}</div>}
          </div>
        )}
        <form onSubmit={handlePasswordChange}>
          <label className="block mb-2 font-semibold text-blue-700 dark:text-blue-200 flex items-center gap-2">
            <IconLock size={20} /> Change Password
          </label>
          <input
            type="password"
            className="w-full px-3 py-2 rounded border border-blue-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-black dark:text-white mb-2"
            value={oldPassword}
            onChange={e => setOldPassword(e.target.value)}
            placeholder="Current password"
            required
          />
          <input
            type="password"
            className="w-full px-3 py-2 rounded border border-blue-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-black dark:text-white mb-2"
            value={newPassword}
            onChange={e => setNewPassword(e.target.value)}
            placeholder="New password"
            required
          />
          <input
            type="password"
            className="w-full px-3 py-2 rounded border border-blue-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-black dark:text-white mb-4"
            value={confirmPassword}
            onChange={e => setConfirmPassword(e.target.value)}
            placeholder="Confirm new password"
            required
          />
          <button
            type="submit"
            className="w-full px-4 py-2 rounded bg-blue-600 text-white hover:bg-blue-700 transition font-semibold"
          >
            Change Password
          </button>
        </form>
      </div>
    </div>
  );
}
