import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';

import { ChannelRoutingModule } from './channel-routing.module';
import { ChannelComponent } from './channel.component';
import { CreateChannelComponent } from './components/create-channel/create-channel.component';
import { EditChannelComponent } from './components/edit-channel/edit-channel.component';
import { ChannelService } from './services/channel.service';
import { NotificationsService } from '../../api/payments/notifications';
import { CreateChannelModule } from '../../shared/components/create-channel/create-channel.module';

@NgModule({
    declarations: [ChannelComponent, EditChannelComponent, CreateChannelComponent],
    imports: [CommonModule, ChannelRoutingModule, CreateChannelModule],
    providers: [NotificationsService, ChannelService],
})
export class ChannelModule {}
