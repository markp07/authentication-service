"use client";

import { useEffect } from "react";
import { redirect } from "next/navigation";

export default function ProfileRedirect() {
  useEffect(() => {
    redirect("/");
  }, []);

  // This component will never render because redirect happens during render
  return null;
}

