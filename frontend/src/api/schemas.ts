import { z } from "zod";

export const CreateMessageSchema = z.object({
  category: z.string().min(1, "Select a category"),
  body: z
    .string()
    .trim()
    .min(1, "Message body is required")
    .max(4000, "Message body must be ≤ 4000 characters"),
});

export type CreateMessageInput = z.infer<typeof CreateMessageSchema>;
