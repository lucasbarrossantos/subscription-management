-- Add renewal_attempts column to subscriptions table
ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS renewal_attempts INTEGER NOT NULL DEFAULT 0;

-- Create index for finding subscriptions to renew
CREATE INDEX IF NOT EXISTS idx_subscription_expiration_status ON subscriptions(expiration_date, status);
