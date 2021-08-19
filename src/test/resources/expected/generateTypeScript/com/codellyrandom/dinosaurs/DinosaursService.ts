import ServiceNetworkClient from "./ServiceNetworkClient"
import { plainToClass, serialize } from "class-transformer"

export class StampedeRequest {
  count: number = 5

  constructor(count: number) {
    this.count = count
  }
}

export class StampedeResponse {
  actual_count: number

  constructor(actual_count: number) {
    this.actual_count = actual_count
  }
}

// Actions that dinosaurs can take.
export class DinosaursService {
  client: ServiceNetworkClient

  constructor(client: ServiceNetworkClient) {
    this.client = client
  }

  // Start a stampede of a certain size.
  async stampede(request: StampedeRequest): Promise<StampedeResponse> {
    const response = await this.client.post("dinosaurs/stampede", serialize(request))
    return plainToClass(StampedeResponse, response.data as JSON)
  }
}

export default DinosaursService
