import ServiceNetworkClient from "./ServiceNetworkClient"
import { plainToClass, serialize } from "class-transformer"

export class StampedeRequest {
  count: number = 5

  constructor(
    count: number = 5
  ) {
    this.count = count
  }
}

export class StampedeResponse {
  actual_count: number

  constructor(
    actual_count: number
  ) {
    this.actual_count = actual_count
  }
}

export default class DinosaursService {
  client: ServiceNetworkClient
  
  constructor(client: ServiceNetworkClient) {
    this.client = client
  }

  async stampede(request: StampedeRequest): Promise<StampedeResponse> {
    const response = await this.client.post("dinosaurs/stampede", serialize(request))
    return plainToClass(StampedeResponse, response.data as Map<string, any>)
  }
}