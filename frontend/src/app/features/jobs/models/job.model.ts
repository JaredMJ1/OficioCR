export interface JobListing {
  id: string;
  title: string;
  description: string;
  category: string;
  location: string;
  createdAt: string;
  authorName: string;
  authorId: string;
  acceptedById: string | null;
  acceptedByName: string | null;
}
