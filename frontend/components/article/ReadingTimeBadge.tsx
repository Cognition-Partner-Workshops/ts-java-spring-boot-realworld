import React from "react";

interface ReadingTimeBadgeProps {
  minutes?: number;
}

const ReadingTimeBadge = ({ minutes }: ReadingTimeBadgeProps) => {
  if (typeof minutes !== "number" || !Number.isFinite(minutes) || minutes < 1) {
    return null;
  }

  return (
    <span
      className="reading-time-badge"
      data-testid="reading-time-badge"
      title="Estimated reading time"
    >
      <i className="ion-clock" /> {minutes} min read
    </span>
  );
};

export default ReadingTimeBadge;
