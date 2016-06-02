// Copyright 2016 The Domain Registry Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package google.registry.model.pricing;

import com.google.common.base.Optional;

import org.joda.money.Money;
import org.joda.time.DateTime;

/** A plugin interface for premium pricing engines. */
public interface PricingEngine {

  /**
   * Returns the prices for the given fully qualified domain name at the given time.
   *
   * <p>Note that the fullyQualifiedDomainName must only contain a single part left of the TLD, i.e.
   * subdomains are not allowed, but multi-part TLDs are.
   */
  public DomainPrices getDomainPrices(String fullyQualifiedDomainName, DateTime priceTime);

  /**
   * A class containing information on prices for a specific domain name.
   *
   * <p>Any implementation of PricingEngine is responsible for determining all of these.
   */
  public static class DomainPrices {

    private boolean isPremium;
    // TODO(b/26901539): Refactor return values to support an arbitrary list of costs for each of
    // create, renew, restore, and transfer.
    private Money createCost;
    private Money renewCost;
    private Optional<Money> oneTimeFee;
    private Optional<String> feeClass;

    static DomainPrices create(
        boolean isPremium,
        Money createCost,
        Money renewCost,
        Optional<Money> oneTimeFee,
        Optional<String> feeClass) {
      DomainPrices instance = new DomainPrices();
      instance.isPremium = isPremium;
      instance.createCost = createCost;
      instance.renewCost = renewCost;
      instance.oneTimeFee = oneTimeFee;
      instance.feeClass = feeClass;
      return instance;
    }

    /** Returns whether the domain is premium. */
    public boolean isPremium() {
      return isPremium;
    }

    /** Returns the cost to create the domain. */
    public Money getCreateCost() {
      return createCost;
    }

    /** Returns the cost to renew the domain. */
    public Money getRenewCost() {
      return renewCost;
    }

    /**
     * Returns the one time fee to register a domain if there is one.
     *
     * <p>This is primarily used for EAP registration fees.
     */
    public Optional<Money> getOneTimeFee() {
      return oneTimeFee;
    }

    /** Returns the fee class of the cost (used for the Fee extension). */
    public Optional<String> getFeeClass() {
      return feeClass;
    }
  }
}