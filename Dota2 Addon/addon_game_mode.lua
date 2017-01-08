if CAddonTemplateGameMode == nil then
	CAddonTemplateGameMode = class({})
end
require("json_helpers")
require("hero_tools")
function Precache( context )
	--[[
		Precache things we know we'll use.  Possible file types include (but not limited to):
			PrecacheResource( "model", "*.vmdl", context )
			PrecacheResource( "soundfile", "*.vsndevts", context )
			PrecacheResource( "particle", "*.vpcf", context )
			PrecacheResource( "particle_folder", "particles/folder", context )
	]]
end

-- Create the game mode when we activate
function Activate()
	GameRules.AddonTemplate = CAddonTemplateGameMode()
	GameRules.AddonTemplate:InitGameMode()
end

function CAddonTemplateGameMode:InitGameMode()
	print( "Template addon is loaded." )
	counter=0;
	GameRules:GetGameModeEntity():SetThink( "OnThink", self, "GlobalThink", 0.33 )
end

-- Evaluate the state of the game
function CAddonTemplateGameMode:OnThink()
	
	if(counter==0) then
			Tutorial:AddBot( "npc_dota_hero_lone_druid", "mid", "hard", false );
			counter = counter+1
		end
	if GameRules:State_Get() == DOTA_GAMERULES_STATE_GAME_IN_PROGRESS then
		print( "Game started" )
		local myHero = Entities:FindByClassname(nil, "npc_dota_hero_lina")		
		local heroEntity = EntIndexToHScript(myHero:GetEntityIndex())
		self:BotUpdating(heroEntity)
		--local myhero= Entities:FindAllByClassname( "npc_dota_hero_lina")
		--print(myhero:GetName())
	elseif GameRules:State_Get() == DOTA_GAMERULES_STATE_PRE_GAME then
		
		
	elseif GameRules:State_Get() >= DOTA_GAMERULES_STATE_POST_GAME then
		return nil
	end
	return 1
end