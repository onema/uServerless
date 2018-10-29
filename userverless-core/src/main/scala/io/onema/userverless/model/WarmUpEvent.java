/*
   This file is part of the ONEMA lambda-mailer Package.
   For the full copyright and license information,
   please view the LICENSE file that was distributed
   with this source code.

   copyright (c) 2018, Juan Manuel Torres (http://onema.io)

   @author Juan Manuel Torres <software@onema.io>
  */
package io.onema.userverless.model;

public class WarmUpEvent {
    private boolean warmup;

    public boolean getWarmup () {
        return warmup;
    }

    public void setWarmup (String warmup) {
        this.warmup = Boolean.valueOf(warmup);
    }

    @Override
    public String toString() {
        return "ClassPojo [warmup = "+warmup+"]";
    }
}
