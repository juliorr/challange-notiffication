import type { components } from "./types.generated";
import type {
  CatalogItem,
  CreateMessageResponse,
  NotificationView,
  PageResponse,
} from "./types";

type GeneratedCatalogItem = components["schemas"]["CatalogItem"];
type GeneratedCreateMessageResponse =
  components["schemas"]["CreateMessageResponse"];
type GeneratedNotificationView = components["schemas"]["NotificationView"];
type GeneratedPageResponse =
  components["schemas"]["PageResponseNotificationView"];

type Assert<_T extends true> = true;

type CatalogHasCode = "code" extends keyof GeneratedCatalogItem ? true : false;
type CatalogHasName = "name" extends keyof GeneratedCatalogItem ? true : false;
type CreateHasId = "id" extends keyof GeneratedCreateMessageResponse
  ? true
  : false;
type CreateHasFanout =
  "fanoutCount" extends keyof GeneratedCreateMessageResponse ? true : false;
type NotificationHasId =
  "id" extends keyof GeneratedNotificationView ? true : false;
type NotificationHasStatus =
  "status" extends keyof GeneratedNotificationView ? true : false;
type PageHasItems =
  "items" extends keyof GeneratedPageResponse ? true : false;
type PageHasCursor =
  "nextCursor" extends keyof GeneratedPageResponse ? true : false;

export type AlignmentChecks = [
  Assert<CatalogHasCode>,
  Assert<CatalogHasName>,
  Assert<CreateHasId>,
  Assert<CreateHasFanout>,
  Assert<NotificationHasId>,
  Assert<NotificationHasStatus>,
  Assert<PageHasItems>,
  Assert<PageHasCursor>,
];

export type PublicRefs = [
  CatalogItem,
  CreateMessageResponse,
  NotificationView,
  PageResponse<NotificationView>,
];
